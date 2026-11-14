package sgl.bazel.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;

/**
 * Packs typed drawable variants into texture-atlas pages.
 *
 * <p>The tool reads the drawable manifests written by the SGL asset rules and the processed
 * drawable files, packs each density separately with a deterministic shelf packer, writes the
 * pages, and writes one replacement manifest. Every drawable keeps its standalone path; packed
 * variants gain an {@code atlas} object. Manifest categories other than drawables are passed
 * through unchanged.
 *
 * <p>Layout of one page: {@code padding} transparent pixels along the top and left border, then
 * cells separated by {@code padding} pixels. A cell is the image surrounded by {@code extrusion}
 * pixels that repeat its edge and corner pixels.
 */
final class TextureAtlasTool {
    static final int SCHEMA_VERSION = 1;

    private TextureAtlasTool() {}

    /** Usage: {@code atlas <config>}. See {@link Config} for the line-based config format. */
    static void run(String[] args) throws IOException {
        if (args.length != 2) throw new IllegalArgumentException("Usage: atlas <config>");
        Config config = Config.read(Path.of(args[1]));
        pack(config);
    }

    /** Tab-separated config lines, written by the {@code sgl_texture_atlas} rule. */
    static final class Config {
        String name;
        int maxTextureSize = 2048;
        int extrusion = 1;
        int padding = 1;
        int maxPages = 1;
        String filter = "nearest";
        String pageResourcePrefix;
        String outputManifest;
        final Set<String> exclude = new HashSet<>();
        final List<String> manifests = new ArrayList<>();
        /** Drawable resource path (for example {@code drawable-mdpi/coin.png}) to input file. */
        final Map<String, String> drawableFiles = new HashMap<>();
        /** Density to the declared page output files, indexed by page number. */
        final Map<String, List<String>> pageOutputs = new TreeMap<>();

        static Config read(Path path) throws IOException {
            Config config = new Config();
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isEmpty()) continue;
                String[] f = line.split("\t", -1);
                switch (f[0]) {
                    case "name": config.name = f[1]; break;
                    case "max_texture_size": config.maxTextureSize = Integer.parseInt(f[1]); break;
                    case "extrusion": config.extrusion = Integer.parseInt(f[1]); break;
                    case "padding": config.padding = Integer.parseInt(f[1]); break;
                    case "max_pages": config.maxPages = Integer.parseInt(f[1]); break;
                    case "filter": config.filter = f[1]; break;
                    case "page_resource_prefix": config.pageResourcePrefix = f[1]; break;
                    case "output_manifest": config.outputManifest = f[1]; break;
                    case "exclude": config.exclude.add(f[1]); break;
                    case "manifest": config.manifests.add(f[1]); break;
                    case "drawable": config.drawableFiles.put(f[1], f[2]); break;
                    case "page":
                        List<String> pages = config.pageOutputs.computeIfAbsent(f[1], d -> new ArrayList<>());
                        if (Integer.parseInt(f[2]) != pages.size()) throw new IllegalArgumentException("Pages must be listed in order: " + line);
                        pages.add(f[3]);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown atlas config line: " + line);
                }
            }
            if (config.name == null || config.pageResourcePrefix == null || config.outputManifest == null) {
                throw new IllegalArgumentException("Atlas config needs name, page_resource_prefix and output_manifest");
            }
            if (config.maxTextureSize <= 0 || config.extrusion < 0 || config.padding < 0 || config.maxPages <= 0) {
                throw new IllegalArgumentException("Invalid atlas dimensions");
            }
            if (!config.filter.equals("nearest") && !config.filter.equals("linear")) {
                throw new IllegalArgumentException("Atlas filter must be nearest or linear: " + config.filter);
            }
            return config;
        }
    }

    /** One drawable variant to place. */
    static final class Item {
        final String name;
        final BufferedImage image;
        final Map<String, Object> variant;
        int page;
        int x;
        int y;

        Item(String name, BufferedImage image, Map<String, Object> variant) {
            this.name = name;
            this.image = image;
            this.variant = variant;
        }
    }

    /** Placement result of a shelf packer run, exposed for tests. */
    static final class Placement {
        final int pageCount;
        final int[] pageWidths;
        final int[] pageHeights;

        Placement(int pageCount, int[] pageWidths, int[] pageHeights) {
            this.pageCount = pageCount;
            this.pageWidths = pageWidths;
            this.pageHeights = pageHeights;
        }
    }

    @SuppressWarnings("unchecked")
    static void pack(Config config) throws IOException {
        Map<String, Object> merged = new LinkedHashMap<>();
        merged.put("atlasVersion", (long) SCHEMA_VERSION);
        Set<String> names = new HashSet<>();
        Map<String, List<Item>> itemsByDensity = new TreeMap<>();
        // Sorting keeps the output independent of the order of the rule's inputs.
        List<String> manifestPaths = new ArrayList<>(config.manifests);
        manifestPaths.sort(null);
        for (String manifestPath : manifestPaths) {
            Map<String, Object> manifest = (Map<String, Object>) Json.parse(Files.readString(Path.of(manifestPath), StandardCharsets.UTF_8));
            if (manifest.containsKey("atlasVersion")) {
                throw new IllegalArgumentException("Drawables in " + manifestPath + " are already part of a texture atlas");
            }
            for (Map.Entry<String, Object> entry : manifest.entrySet()) {
                List<Object> values = (List<Object>) merged.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                values.addAll((List<Object>) entry.getValue());
            }
        }
        for (Object drawableValue : (List<Object>) merged.getOrDefault("drawables", new ArrayList<>())) {
            Map<String, Object> drawable = (Map<String, Object>) drawableValue;
            String name = (String) drawable.get("name");
            if (!names.add(name)) throw new IllegalArgumentException("Duplicate drawable asset name " + name);
            if (!drawable.containsKey("variants")) {
                List<Object> variants = new ArrayList<>();
                Map<String, Object> variant = new LinkedHashMap<>();
                variant.put("density", "mdpi");
                variant.put("path", drawable.get("path"));
                variants.add(variant);
                drawable.put("variants", variants);
            }
            ((List<Object>) drawable.get("variants")).sort(Comparator.comparing(
                v -> (String) ((Map<String, Object>) v).get("density")));
            if (config.exclude.contains(name)) continue;
            for (Object variantValue : (List<Object>) drawable.get("variants")) {
                Map<String, Object> variant = (Map<String, Object>) variantValue;
                String density = (String) variant.get("density");
                String path = (String) variant.get("path");
                if (!path.toLowerCase().endsWith(".png")) {
                    throw new IllegalArgumentException("Texture atlases only accept PNG drawables; add " + name + " to exclude to keep " + path + " standalone");
                }
                String file = config.drawableFiles.get(path);
                if (file == null) throw new IllegalArgumentException("No input file for drawable " + name + " variant " + path);
                if (!config.pageOutputs.containsKey(density)) throw new IllegalArgumentException("No pages declared for density " + density);
                BufferedImage image = ImageIO.read(new File(file));
                if (image == null) throw new IOException("Unsupported image format: " + file);
                itemsByDensity.computeIfAbsent(density, d -> new ArrayList<>()).add(new Item(name, image, variant));
            }
        }
        for (String excluded : config.exclude) {
            if (!names.contains(excluded)) throw new IllegalArgumentException("exclude names unknown drawable asset " + excluded);
        }

        for (Map.Entry<String, List<String>> entry : config.pageOutputs.entrySet()) {
            String density = entry.getKey();
            List<String> outputs = entry.getValue();
            List<Item> items = itemsByDensity.getOrDefault(density, new ArrayList<>());
            Placement placement = place(items, config.maxTextureSize, config.extrusion, config.padding);
            if (placement.pageCount > outputs.size()) {
                throw new IllegalArgumentException(
                    "Texture atlas " + config.name + " needs " + placement.pageCount + " pages for density " + density +
                    " but max_pages is " + outputs.size() + "; raise max_pages or max_texture_size");
            }
            BufferedImage[] pages = new BufferedImage[placement.pageCount];
            for (int i = 0; i < placement.pageCount; i++) {
                pages[i] = new BufferedImage(placement.pageWidths[i], placement.pageHeights[i], BufferedImage.TYPE_INT_ARGB);
            }
            for (Item item : items) {
                draw(pages[item.page], item.image, item.x, item.y, config.extrusion);
                Map<String, Object> atlas = new LinkedHashMap<>();
                atlas.put("path", pageResourcePath(config, density, item.page));
                atlas.put("pageWidth", (long) placement.pageWidths[item.page]);
                atlas.put("pageHeight", (long) placement.pageHeights[item.page]);
                atlas.put("x", (long) (item.x + config.extrusion));
                atlas.put("y", (long) (item.y + config.extrusion));
                atlas.put("width", (long) item.image.getWidth());
                atlas.put("height", (long) item.image.getHeight());
                atlas.put("filter", config.filter);
                item.variant.put("atlas", atlas);
            }
            for (int i = 0; i < outputs.size(); i++) {
                // Unused declared pages stay as 1x1 transparent placeholders that nothing references.
                BufferedImage page = i < pages.length ? pages[i] : new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                writePng(page, outputs.get(i));
            }
        }

        Path output = Path.of(config.outputManifest);
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        // Canonical category and asset order, independent of manifest filenames
        // and declaration order. Keep the version first for easy inspection.
        Map<String, Object> canonical = new LinkedHashMap<>();
        canonical.put("atlasVersion", (long) SCHEMA_VERSION);
        for (String category : new TreeMap<>(merged).keySet()) {
            if (category.equals("atlasVersion")) continue;
            List<Object> assets = (List<Object>) merged.get(category);
            assets.sort(Comparator.comparing(a -> (String) ((Map<String, Object>) a).get("name")));
            canonical.put(category, assets);
        }
        Files.writeString(output, Json.write(canonical), StandardCharsets.UTF_8);
    }

    static String pageResourcePath(Config config, String density, int page) {
        return config.pageResourcePrefix + "/" + density + "/" + String.format("page-%03d.png", page);
    }

    /**
     * Shelf packing without rotation. Items are sorted by descending padded height, descending
     * padded width, then name. Each item goes to the first page, then first shelf, that fits it;
     * otherwise a new shelf opens on the first page with vertical room, otherwise a new page.
     * Page sizes are the used extent plus the trailing padding.
     */
    static Placement place(List<Item> items, int maxSize, int extrusion, int padding) {
        items.sort(Comparator
            .comparingInt((Item i) -> -(i.image.getHeight() + 2 * extrusion))
            .thenComparingInt(i -> -(i.image.getWidth() + 2 * extrusion))
            .thenComparing(i -> i.name));
        List<List<int[]>> shelvesByPage = new ArrayList<>(); // shelf: {y, height, nextX}
        List<int[]> pageCursor = new ArrayList<>(); // {nextShelfY, usedWidth, usedHeight}
        for (Item item : items) {
            int cw = item.image.getWidth() + 2 * extrusion;
            int ch = item.image.getHeight() + 2 * extrusion;
            if (padding + cw + padding > maxSize || padding + ch + padding > maxSize) {
                throw new IllegalArgumentException(
                    "Drawable " + item.name + " (" + item.image.getWidth() + "x" + item.image.getHeight() +
                    " plus extrusion and padding) does not fit in a " + maxSize + " atlas page; add it to exclude to keep it standalone");
            }
            boolean placed = false;
            for (int p = 0; p < shelvesByPage.size() && !placed; p++) {
                for (int[] shelf : shelvesByPage.get(p)) {
                    if (ch <= shelf[1] && shelf[2] + cw + padding <= maxSize) {
                        placeAt(item, p, shelf, pageCursor.get(p), cw, ch, padding);
                        placed = true;
                        break;
                    }
                }
            }
            for (int p = 0; p < shelvesByPage.size() && !placed; p++) {
                int[] cursor = pageCursor.get(p);
                if (cursor[0] + ch + padding <= maxSize) {
                    int[] shelf = {cursor[0], ch, padding};
                    shelvesByPage.get(p).add(shelf);
                    cursor[0] += ch + padding;
                    placeAt(item, p, shelf, cursor, cw, ch, padding);
                    placed = true;
                }
            }
            if (!placed) {
                int p = shelvesByPage.size();
                int[] cursor = {padding, 0, 0};
                int[] shelf = {padding, ch, padding};
                shelvesByPage.add(new ArrayList<>(List.of(shelf)));
                pageCursor.add(cursor);
                cursor[0] += ch + padding;
                placeAt(item, p, shelf, cursor, cw, ch, padding);
            }
        }
        int count = pageCursor.size();
        int[] widths = new int[count];
        int[] heights = new int[count];
        for (int p = 0; p < count; p++) {
            widths[p] = pageCursor.get(p)[1];
            heights[p] = pageCursor.get(p)[2];
        }
        return new Placement(count, widths, heights);
    }

    private static void placeAt(Item item, int page, int[] shelf, int[] cursor, int cw, int ch, int padding) {
        item.page = page;
        item.x = shelf[2];
        item.y = shelf[0];
        shelf[2] += cw + padding;
        cursor[1] = Math.max(cursor[1], item.x + cw + padding);
        cursor[2] = Math.max(cursor[2], item.y + ch + padding);
    }

    /** Copies {@code image} with its content at {@code (x + extrusion, y + extrusion)}, repeating edges. */
    private static void draw(BufferedImage page, BufferedImage image, int x, int y, int extrusion) {
        int w = image.getWidth();
        int h = image.getHeight();
        for (int dy = -extrusion; dy < h + extrusion; dy++) {
            int sy = Math.max(0, Math.min(h - 1, dy));
            for (int dx = -extrusion; dx < w + extrusion; dx++) {
                int sx = Math.max(0, Math.min(w - 1, dx));
                page.setRGB(x + extrusion + dx, y + extrusion + dy, image.getRGB(sx, sy));
            }
        }
    }

    private static void writePng(BufferedImage image, String path) throws IOException {
        File output = new File(path);
        File parent = output.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create output directory: " + parent);
        }
        if (!ImageIO.write(image, "png", output)) throw new IOException("No PNG writer for " + path);
    }

    /** Minimal JSON support for the asset manifests, which only hold objects, arrays, strings and integers. */
    static final class Json {
        private final String text;
        private int pos;

        private Json(String text) {
            this.text = text;
        }

        static Object parse(String text) {
            Json json = new Json(text);
            Object value = json.value();
            json.whitespace();
            if (json.pos != text.length()) throw json.error("Trailing characters");
            return value;
        }

        private Object value() {
            whitespace();
            if (pos >= text.length()) throw error("Unexpected end of JSON");
            char c = text.charAt(pos);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (text.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (text.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (text.startsWith("null", pos)) { pos += 4; return null; }
            return number();
        }

        private Map<String, Object> object() {
            Map<String, Object> result = new LinkedHashMap<>();
            pos++;
            whitespace();
            if (peek() == '}') { pos++; return result; }
            while (true) {
                whitespace();
                String key = string();
                whitespace();
                expect(':');
                result.put(key, value());
                whitespace();
                if (peek() == ',') { pos++; continue; }
                expect('}');
                return result;
            }
        }

        private List<Object> array() {
            List<Object> result = new ArrayList<>();
            pos++;
            whitespace();
            if (peek() == ']') { pos++; return result; }
            while (true) {
                result.add(value());
                whitespace();
                if (peek() == ',') { pos++; continue; }
                expect(']');
                return result;
            }
        }

        private String string() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (true) {
                if (pos >= text.length()) throw error("Unterminated string");
                char c = text.charAt(pos++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                char e = text.charAt(pos++);
                switch (e) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: throw error("Invalid escape");
                }
            }
        }

        private Long number() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            if (start == pos || (pos < text.length() && ".eE".indexOf(text.charAt(pos)) >= 0)) throw error("Only integer numbers are supported");
            return Long.parseLong(text.substring(start, pos));
        }

        private char peek() {
            return pos < text.length() ? text.charAt(pos) : '\0';
        }

        private void expect(char c) {
            if (peek() != c) throw error("Expected '" + c + "'");
            pos++;
        }

        private void whitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at offset " + pos);
        }

        @SuppressWarnings("unchecked")
        static String write(Object value) {
            StringBuilder out = new StringBuilder();
            write(value, out);
            return out.toString();
        }

        @SuppressWarnings("unchecked")
        private static void write(Object value, StringBuilder out) {
            if (value == null) {
                out.append("null");
            } else if (value instanceof String) {
                writeString((String) value, out);
            } else if (value instanceof Map) {
                out.append('{');
                boolean first = true;
                for (Map.Entry<String, Object> e : ((Map<String, Object>) value).entrySet()) {
                    if (!first) out.append(',');
                    first = false;
                    writeString(e.getKey(), out);
                    out.append(':');
                    write(e.getValue(), out);
                }
                out.append('}');
            } else if (value instanceof List) {
                out.append('[');
                boolean first = true;
                for (Object item : (List<Object>) value) {
                    if (!first) out.append(',');
                    first = false;
                    write(item, out);
                }
                out.append(']');
            } else {
                out.append(value);
            }
        }

        private static void writeString(String s, StringBuilder out) {
            out.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"': out.append("\\\""); break;
                    case '\\': out.append("\\\\"); break;
                    case '\n': out.append("\\n"); break;
                    case '\r': out.append("\\r"); break;
                    case '\t': out.append("\\t"); break;
                    default:
                        if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                        else out.append(c);
                }
            }
            out.append('"');
        }
    }
}
