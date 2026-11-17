# Kitchen timer studies

These are three proposed directions. No final product name or visual direction has been selected.

## Shared construction

Each 360 × 780 study uses an app-sized physical housing, recessed time readout, rotary control, full-hour counter, raised Start/Reset buttons and a physical sound/vibration switch. Lighting comes from the upper left. The timer geometry and shading are editable CSS and SVG, with no flattened timer images. Barlow Condensed is embedded in the HTML; its license is in assets/OFL.txt. The file runs offline without a build step.

## Proposed materials

| Study | Housing | Mechanism | Colours |
| --- | --- | --- | --- |
| Cream mechanical | Warm moulded ABS | Raised rotating grip; red hour-layer ring | #eeeadd, #dedbcf, #b7372b |
| Enamel & chrome | Satin pink enamel | Concentric metal bezel, glazed face, small domed knob | #e9b8b0, #d69d98, #d86629 |
| The red disc | Charcoal rubber; ivory paper dial | Recessed square face, large coloured disc, small cap | #383e39, #292e2b, #ece9d9, #c73b2d |

## Hour layers

Requested by the user during development: each full turn leaves a colour layer; additional minutes use the next darker shade. For 2 h 25, the full disc has the second layer's shade and a 150° sector has the third. At exact hours the partial sector has zero area. Countdown reverses these layers continuously across hour boundaries. Higher-hour shade differences taper, so the exact time and full-hour counter remain the authoritative values.

## Interaction

Drag anywhere on the dial, including starting on its centre cap. Continuous rotation is accumulated across 12 o’clock. Arrow keys adjust one minute; Shift adjusts five. Enter on a focused dial or tapping the readout opens exact hour/minute entry. Start/Pause/Resume, Reset and a repeating foreground alarm work. Turning sound off selects vibration on supported browsers. The presets above the studies synchronize their duration for material comparison.

## Scope

Prototype duration range is 0–23 h 59 min, an implementation assumption. No background alarm reliability or installed Android app is claimed. Browser vibration and audio depend on browser support and user activation. The timer keeps a deadline while the page remains open. The host comparison page stacks studies on narrow screens.

## Validation

Verified in Chrome: exact time entry; keyboard control; countdown/pause/reset; two complete dial turns; centre cap dragging; sound switch; study filtering; dialog focus restoration; alarm preview/stop; hour-layer presets and exact-hour boundaries. No horizontal overflow or clipped device content at 320, 390, 900, 1100 and 1440px. Captures are in review/. Sound synthesis and vibration calls exist; physical Android vibration has not been device-tested.
