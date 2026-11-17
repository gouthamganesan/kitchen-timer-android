# UI plan

2026-11-17. Ownership: ui/ Kotlin, UI drawables, these role documents. Shared model and Activity remain integration-owned.

1. Port approved hardware surfaces to native Canvas/Compose, preserving color, typography, lighting and relative geometry.
2. Wire winding with multi-turn unwrapped angles, user-only haptic policy, independent knob taps, accessible adjustment, primary/reset and alarm rocker.
3. Build Material duration picker (dial/keyboard), regular-only fixed presets, settings sheet and permission affordances.
4. Compile with integrated project, fix UI failures; integration drives emulator/device and sends rendered evidence for fixes.

Acceptance: all approved regular/focus/theme states and actions work, no quick set in focus, no countdown haptics, 48dp touch targets, usable compact/landscape/large-font sheets. No WebView, no fake status bar.


Landscape regression acceptance (2026-11-17): use measured LCD height plus full88dp button row and48dp rocker to choose landscape spacing; tested320×568dp/font1.3 must show the complete rocker at rest, smaller windows must scroll it fully into view, settings access must remain reachable. Preserve approved portrait geometry and balance.


Rocker press refinement (2026-11-17): suppress the rectangular Material indication around the sound/vibration row; retain its minimum48dp hit target, accessible switch semantics, active-icon state, physical slider movement and snap haptic. Integration owns the pressed-state screenshot and toggle regression check.
