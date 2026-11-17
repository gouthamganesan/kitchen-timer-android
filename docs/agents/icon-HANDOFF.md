# Icon handoff

Public source package: [artwork README](../../artwork/kitchen-timer-icon/README.md). It contains the original generated image, editable SVG and semantic layers, review PNG, spec and prompt provenance. Android color/adaptive/monochrome vectors and legacy density PNGs are committed under `app/src/main/res`.

The icon depicts an arched freestanding kitchen timer with feet, ivory dial, red time sector and winding paddle. It deliberately simplifies photographic shading into editable gradients; no embedded raster or pixel-perfect trace is claimed.

Local verification passed geometry, editability and Android mask checks. All significant foreground alpha was inside the 66dp safe circle on a 108dp canvas; measured maximum radius was 32.78dp. The native launcher rendering was also inspected. See [official adaptive icon guidance](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).

Edit the public SVG and layers using vector tools; keep native launcher resources in sync and inspect the common masks at actual icon size. Temporary authoring/export utilities and raw reports are local-only, not required to build the app. The original environment's authoring skill is not a dependency of this public repository.
