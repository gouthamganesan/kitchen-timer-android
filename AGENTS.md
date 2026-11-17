# Kitchen Timer agent instructions

Read `docs/REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/PLAN.md`, `docs/PROGRESS.md` and `docs/HANDOFF.md` before work. The README explains the design process; `reference/approved-prototype.html` is the frozen visual direction, with later native refinements recorded in requirements.

You are not alone in this codebase. Preserve others' changes and coordinate shared contracts. Each implementation agent maintains `docs/agents/<role>-PLAN.md`, `-PROGRESS.md` and `-HANDOFF.md`: ownership, decisions, exact checks, blockers and continuation steps. Never claim checks passed without running them.

## Product boundaries

Use native Kotlin, Compose and Canvas. Keep the app offline: no accounts, analytics or Internet permission. App name is Kitchen Timer. Source publication under MIT is authorized; store releases and signing/distribution changes require an explicit request.

Preserve the hardware visual language in DESIGN.md. Regular dark mode lights the LCD and dial while retaining the casing. Focus keeps the earlier ivory/red or charcoal/amber face, has no Quick set, and uses a single centre tap. Theme is general and defaults to Follow system.

Minute, five-minute, hour and end-stop haptics originate only from deliberate time-setting gestures. Countdown, restoration, presets, notification drawing and theme changes never generate dial ticks. Respect system touch-feedback preferences; alarm vibration is separate.

## Development and verification

Run `./scripts/check.sh` for build, tests and lint. Use JDK 17+ and Android SDK 36. `local.properties` is machine-specific and ignored. Use Maestro first for device interactions and adb for installation/logs/system diagnostics; always explicitly select the device. Coordinate before driving a personal phone or disrupting a real timer.

Check compact portrait, landscape and increased font size after layout changes. Preserve action accessibility and stable LCD/dial geometry. Physical motor/speaker quality requires device observations; emulator results are insufficient.

## Public repository hygiene

Keep credentials, signing material, APKs, raw logs, device identifiers and personal paths out of Git. `artifacts/`, `.local/` and artwork `tmp/` folders are local-only. Put reviewed public screenshots in `docs/images/`. Run `python3 scripts/check_publication.py` before publishing. Preserve original records locally when preparing a public summary; update the test matrix honestly.
