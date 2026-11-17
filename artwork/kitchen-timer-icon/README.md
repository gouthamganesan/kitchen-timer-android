# Kitchen Timer icon

An arched, freestanding timer with an ivory face, red remaining-time sector and winding paddle. It preserves the app's physical identity without tiny lettering.

- `kitchen-timer-icon.origin.png`: original image-generated concept, preserved unchanged.
- `kitchen-timer-icon.svg`: editable paths, primitives and gradients; no embedded raster.
- `kitchen-timer-icon.preview.png`: rounded review rendering.
- `layers/`: separate housing, dial, sector, scale and paddle geometry.
- `spec.json`: semantic geometry and authoring metadata.
- `provenance.json`: generation prompt and reconstruction decisions.

The vector is a deliberate simplification of the generated material treatment, not a pixel-perfect trace. The SVG and layers are editable with normal vector tools; the original authoring utility is not a build dependency.

Android launcher resources are committed under `app/src/main/res` at the repository root: color adaptive icons, an Android 13+ monochrome icon and legacy density PNGs. Changing the source SVG does not automatically regenerate those resources. Keep them in sync and inspect circle, rounded-square and themed masks when revising the artwork. Temporary export tools and raw comparison reports remain local and are excluded from Git.
