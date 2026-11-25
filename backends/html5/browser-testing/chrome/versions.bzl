"""What the bundled Chrome is made of. `resolve.py` writes this file."""

# Chrome for Testing, https://googlechromelabs.github.io/chrome-for-testing/
CHROME_VERSION = "158.0.8127.88"
CHROME_SHA256 = "63401333cc587645bfaeea7f0c90958b327d249251e23954a47f30112ca23d5b"

# Debian bookworm, as of this date.
DEBIAN_SNAPSHOT = "20260901T000000Z"

# The libraries Chrome needs, with theirs down to glibc, and a font.
DEBIAN_PACKAGES = {
    "pool/main/a/alsa-lib/libasound2_1.2.8-1+b1_amd64.deb": "44c77b076a7b11ae99712439022d822245b1994c435da564ebd320bb676faf4c",
    "pool/main/a/at-spi2-core/libatk-bridge2.0-0_2.46.0-5_amd64.deb": "fbb07714fdd3fdee7b63fd8595053f4fa8021d8e350a5de6281c6ce7cd4f158f",
    "pool/main/a/at-spi2-core/libatk1.0-0_2.46.0-5_amd64.deb": "909dd661e03760a9ba1529bfc772f259cec8431999ee38b7c5521186237cdd98",
    "pool/main/a/at-spi2-core/libatspi2.0-0_2.46.0-5_amd64.deb": "1c0332f3b712a8e5ee56800bbcab83a8f92098874094f2b0bdfd6b7fd45e8b83",
    "pool/main/d/dbus/libdbus-1-3_1.14.10-1~deb12u1_amd64.deb": "18ee0ce5fab9f7b671e87da1e9fa18660e36e04a3402f24bdb8635e0ba1d35f6",
    "pool/main/e/expat/libexpat1_2.5.0-1+deb12u2_amd64.deb": "2255e62fc22a86d2c544b8a3f516da9aee19383ad5742722ab4ce7f66a30dbc8",
    "pool/main/f/fonts-dejavu/fonts-dejavu-core_2.37-6_all.deb": "8892669e51aab4dc56682c8e39d8ddb7d70fad83c369344e1e240bf3ca22bb76",
    "pool/main/g/gcc-12/libgcc-s1_12.2.0-14+deb12u1_amd64.deb": "3016e62cb4b7cd8038822870601f5ed131befe942774d0f745622cc77d8a88f7",
    "pool/main/g/glib2.0/libglib2.0-0_2.74.6-2+deb12u9_amd64.deb": "7ff85197685d89e150e342b29a59aab1beee400050ba7da73de81cd999ffee5a",
    "pool/main/g/glibc/libc6_2.36-9+deb12u14_amd64.deb": "ba4f88f73dbc3ae9055f3c20f4523bfdbaf1ad13ff95e258924f77d20b4fbedf",
    "pool/main/l/lz4/liblz4-1_1.9.4-1_amd64.deb": "64cde86cef1deaf828bd60297839b59710b5cd8dc50efd4f12643caaee9389d3",
    "pool/main/libb/libbsd/libbsd0_0.11.7-2_amd64.deb": "bb31cc8b40f962a85b2cec970f7f79cc704a1ae4bad24257a822055404b2c60b",
    "pool/main/libc/libcap2/libcap2_2.66-4+deb12u3+b1_amd64.deb": "8d684c673e7483802a5c447834b0df6fce006eb3a7109e1be412f5ad425bb24f",
    "pool/main/libd/libdrm/libdrm2_2.4.114-1+b1_amd64.deb": "be18fb670797ba32da9628cf3e8acd83160d8db8c8dd842501dd8e401c3b5371",
    "pool/main/libf/libffi/libffi8_3.4.4-1_amd64.deb": "6d9f6c25c30efccce6d4bceaa48ea86c329a3432abb360a141f76ac223a4c34a",
    "pool/main/libg/libgcrypt20/libgcrypt20_1.10.1-3+deb12u1_amd64.deb": "e7a56552139c693904a6f9712234c05fe918353273a7cbdb5e62f423ad71bb97",
    "pool/main/libg/libgpg-error/libgpg-error0_1.46-1_amd64.deb": "89944ee11d7370ce6ef46fc52f094c4a6512eff8943ec4c6ebefeae6360ceada",
    "pool/main/libm/libmd/libmd0_1.0.4-2_amd64.deb": "03539fd30c509e27101d13a56e52eda9062bdf1aefe337c07ab56def25a13eab",
    "pool/main/libs/libselinux/libselinux1_3.4-1+b6_amd64.deb": "2b07f5287b9105f40158b56e4d70cc1652dac56a408f3507b4ab3d061eed425f",
    "pool/main/libx/libx11/libx11-6_1.8.4-2+deb12u2_amd64.deb": "d88c973e79fd9b65838d77624142952757e47a6eb1a58602acf0911cf35989f4",
    "pool/main/libx/libxau/libxau6_1.0.9-1_amd64.deb": "679db1c4579ec7c61079adeaae8528adeb2e4bf5465baa6c56233b995d714750",
    "pool/main/libx/libxcb/libxcb1_1.15-1_amd64.deb": "fdc61332a3892168f3cc9cfa1fe9cf11a91dc3e0acacbc47cbc50ebaa234cc71",
    "pool/main/libx/libxcomposite/libxcomposite1_0.4.5-1_amd64.deb": "4c26ebf519d2ebc22fc1416dee45e12c4c4ef68aa9b2ed890356830df42b652a",
    "pool/main/libx/libxdamage/libxdamage1_1.1.6-1_amd64.deb": "fcdf7e4c29b307ba8711c864e1e646d76315a8f55121198143653e56592fa8a7",
    "pool/main/libx/libxdmcp/libxdmcp6_1.1.2-3_amd64.deb": "ecb8536f5fb34543b55bb9dc5f5b14c9dbb4150a7bddb3f2287b7cab6e9d25ef",
    "pool/main/libx/libxext/libxext6_1.3.4-1+b1_amd64.deb": "504b7be9d7df4f6f4519e8dd4d6f9d03a9fb911a78530fa23a692fba3058cba6",
    "pool/main/libx/libxfixes/libxfixes3_6.0.0-2_amd64.deb": "1cd616396ff2ecae77e6e8b5b7695d414f0146de2d147837a2a02165f99e1a2c",
    "pool/main/libx/libxi/libxi6_1.8-1+b1_amd64.deb": "6c2fb5f241b311788990122c6e693915867cd8ed9d6da2e3524aec7ed3cdb4c0",
    "pool/main/libx/libxkbcommon/libxkbcommon0_1.5.0-1_amd64.deb": "e3fe045b9a33a101de1c5a912a4a10928db055c3f68930f47eccbb44d7c7d54e",
    "pool/main/libx/libxrandr/libxrandr2_1.5.2-2+b1_amd64.deb": "e2e10b672085ddf1d294cd689ed21bde5899325fd13626c2614b7d7b2d49a808",
    "pool/main/libx/libxrender/libxrender1_0.9.10-1.1_amd64.deb": "b2a6d160578f359220101947c8d67059b44704a251e688de1d0b744fefedfd1a",
    "pool/main/libz/libzstd/libzstd1_1.5.4+dfsg2-5_amd64.deb": "6315b5ac38b724a710fb96bf1042019398cb656718b1522279a5185ed39318fa",
    "pool/main/m/mesa/libgbm1_22.3.6-1+deb12u2_amd64.deb": "4323840f2b7b3e108990848529125d99c0956ae5a854c1e0568ed08c8faeaa2e",
    "pool/main/n/nspr/libnspr4_4.35-1_amd64.deb": "6cca09767e94e4b5d2888ef31ad797bd3e7cec27fbbe98b49b10d4884746ce77",
    "pool/main/n/nss/libnss3_3.87.1-1+deb12u2_amd64.deb": "bb4339de2e76be9862225447c6c24b9d759baba5ee861d86dfb558ebf4474d14",
    "pool/main/p/pcre2/libpcre2-8-0_10.42-1_amd64.deb": "030db54f4d76cdfe2bf0e8eb5f9efea0233ab3c7aa942d672c7b63b52dbaf935",
    "pool/main/s/systemd/libsystemd0_252.39-1~deb12u2_amd64.deb": "013c07514a9e4933b2d5c098018695a6aa42d085dbc483ce2c094a954c8a1b0a",
    "pool/main/s/systemd/libudev1_252.39-1~deb12u2_amd64.deb": "6f7e08f11c4809d2e5998c775f7b423ab5ec2ce0309494b4a2615037052245e2",
    "pool/main/u/util-linux/libblkid1_2.38.1-5+deb12u3_amd64.deb": "5a083377f29191cafcdf0f91ec124349b4a7895b531556633a136e43c6d53943",
    "pool/main/u/util-linux/libmount1_2.38.1-5+deb12u3_amd64.deb": "f46e2d6b006951825adde9757790b6609d7c66c7700f9b68f3db913eaac323c3",
    "pool/main/w/wayland/libwayland-server0_1.21.0-1_amd64.deb": "df0396221d7b794496a687ec61fae82b6465648bc0ab6501ba0a5ed7f56eb8d6",
    "pool/main/x/xz-utils/liblzma5_5.4.1-1+deb12u1_amd64.deb": "f96d8876b53ec89d76a992bc199679b818cf518225a768954d9451fb556a4eb7",
    "pool/main/z/zlib/zlib1g_1.2.13.dfsg-1_amd64.deb": "d7dd1d1411fedf27f5e27650a6eff20ef294077b568f4c8c5e51466dc7c08ce4",
}
