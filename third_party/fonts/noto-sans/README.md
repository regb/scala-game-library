# Noto Sans

`NotoSans-Regular.ttf` and `NotoSans-Bold.ttf` come from the Noto Fonts project:

https://github.com/notofonts/noto-fonts

They are licensed under the SIL Open Font License 1.1. See `LICENSE`.

Sources and checksums:

- `NotoSans-Regular.ttf`: `https://github.com/notofonts/noto-fonts/raw/main/hinted/ttf/NotoSans/NotoSans-Regular.ttf`, SHA-256 `b85c38ecea8a7cfb39c24e395a4007474fa5a4fc864f6ee33309eb4948d232d5`
- `NotoSans-Bold.ttf`: `https://github.com/notofonts/noto-fonts/raw/main/hinted/ttf/NotoSans/NotoSans-Bold.ttf`, SHA-256 `c976e4b1b99edc88775377fcc21692ca4bfa46b6d6ca6522bfda505b28ff9d6a`

SGL uses these files as the source for the checked-in portable Canvas font atlas. Regenerate it from the repository root with:

```bash
mkdir -p /tmp/sgl-font-generator
javac -d /tmp/sgl-font-generator tools/font-atlas-generator/GeneratePortableFontAtlas.java
java -cp /tmp/sgl-font-generator GeneratePortableFontAtlas \
  third_party/fonts/noto-sans/NotoSans-Regular.ttf \
  third_party/fonts/noto-sans/NotoSans-Bold.ttf \
  core/src/main/scala/sgl/PortableFontData.scala
```
