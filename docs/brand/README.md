# Documentation marks

`android.svg` is the Android robot from Simple Icons; `kotlin.svg` is the complete primary light-background logo from the official Kotlin logo bundle. They identify the app's platform and language. See [third-party notices](../../THIRD_PARTY_NOTICES.md) for sources and attribution.

The banner is generated with `python3 docs/generate_banner.py` from the repository root. Install the optional Pillow dependency in `docs/banner-requirements.txt` first. The bundled PNGs are lossless renderings of the adjacent SVG marks; the Android robot is rendered in its standard green. The generator uses the bundled Barlow font and checks every text/image boundary. None of these documentation tools is required to build the app.
