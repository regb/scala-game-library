#!/usr/bin/env python3
import collections
import struct
import sys
import zlib


def pixels(path):
    data = open(path, "rb").read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG")
    offset = 8
    compressed = bytearray()
    width = height = depth = color_type = None
    while offset < len(data):
        length = int.from_bytes(data[offset : offset + 4], "big")
        chunk_type = data[offset + 4 : offset + 8]
        chunk = data[offset + 8 : offset + 8 + length]
        offset += length + 12
        if chunk_type == b"IHDR":
            width, height, depth, color_type, _, _, _ = struct.unpack(">IIBBBBB", chunk)
        elif chunk_type == b"IDAT":
            compressed.extend(chunk)
    if depth != 8 or color_type != 6:
        raise ValueError(f"unsupported PNG depth/color type: {depth}/{color_type}")

    raw = zlib.decompress(compressed)
    stride = width * 4
    previous = bytearray(stride)
    offset = 0
    result = []
    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        current = bytearray(raw[offset : offset + stride])
        offset += stride
        for index in range(stride):
            left = current[index - 4] if index >= 4 else 0
            above = previous[index]
            upper_left = previous[index - 4] if index >= 4 else 0
            if filter_type == 0:
                predictor = 0
            elif filter_type == 1:
                predictor = left
            elif filter_type == 2:
                predictor = above
            elif filter_type == 3:
                predictor = (left + above) // 2
            elif filter_type == 4:
                candidate = left + above - upper_left
                distances = (abs(candidate - left), abs(candidate - above), abs(candidate - upper_left))
                predictor = (left, above, upper_left)[distances.index(min(distances))]
            else:
                raise ValueError(f"unsupported PNG filter: {filter_type}")
            current[index] = (current[index] + predictor) & 0xFF
        result.extend(tuple(current[index : index + 3]) for index in range(0, stride, 4))
        previous = current
    return width, height, result


def main():
    if len(sys.argv) != 3 or sys.argv[1] not in ("colors", "vertical"):
        raise SystemExit(f"usage: {sys.argv[0]} <colors|vertical> <screenshot.png>")
    mode, path = sys.argv[1:]
    width, _, image = pixels(path)
    counts = collections.Counter(image)
    required = {
        "playfield": counts[(16, 48, 24)],
        "snake_blue": counts[(0, 0, 255)],
        "snake_green": counts[(0, 255, 0)],
        "apple_red": counts[(255, 0, 0)],
    }
    print(required)
    if required["playfield"] < 1_000_000 or any(required[name] < 100 for name in ("snake_blue", "snake_green", "apple_red")):
        raise SystemExit("Snake framebuffer colors are missing")
    if mode == "vertical":
        snake = [index for index, color in enumerate(image) if color in ((0, 0, 255), (0, 255, 0))]
        xs = [index % width for index in snake]
        ys = [index // width for index in snake]
        snake_width = max(xs) - min(xs) + 1
        snake_height = max(ys) - min(ys) + 1
        print({"snake_width": snake_width, "snake_height": snake_height})
        if snake_height <= snake_width:
            raise SystemExit("Snake did not turn vertically after pointer input")


if __name__ == "__main__":
    main()
