# Pickup entry point

Start with the [README](../README.md), [requirements](REQUIREMENTS.md), [architecture](ARCHITECTURE.md), [progress](PROGRESS.md) and [test matrix](testing/TEST-MATRIX.md). Individual implementation decisions remain in [agent handoffs](agents).

## Build and layout

The single Android module uses Kotlin and native Compose/Canvas. Run `./scripts/check.sh` with JDK 17+ and Android SDK 36. Package: `dev.goutham.kitchentimer`; label: Kitchen Timer. Current version: 0.1.2, code 3.

- `core/` owns deadline/state transitions.
- `platform/` owns persisted state, alarms, notifications and ringing service.
- `ui/` owns hardware rendering, gestures, picker, presets and settings.
- `haptics/` owns native action feedback and input-only dial policy.
- `MainActivity.kt` owns lifecycle, permission requests, theme and visible-only Stay awake.

Source code is under `app/src/main/java/dev/goutham/kitchentimer`. Contracts and ownership are described in the architecture doc.

## Latest refinement

Version 0.1.2 removes the default rectangular ripple around the sound/vibration row. Its physical switch position, active icon, 48dp target, accessibility and snap haptic remain. Earlier changes added the new icon/name, upward screen swipe, balanced lower spacing, compact landscape fit and constant LCD height.

The user requested installation on their reconnected phone and public GitHub publication under MIT. Publication progress is tracked in [the publication handoff](agents/publication-HANDOFF.md). Raw install evidence and APK copies remain local under ignored `artifacts/`; do not publish those records.

## Validation and limits

The test matrix separates unit, emulator and device results. Use [Maestro](../maestro/README.md) on an explicitly chosen device. Phone installation does not establish haptic feel, audio, DND, lock/Doze or OEM behavior. Complete permission-denial/revocation, actual reboot and full accessibility checks remain listed.

The notification illustration is a snapshot; native Chronometer supplies the live countdown. Android force-stop cancels scheduled work until relaunch. Settings stay local and screen-awake only affects the visible activity.

## Assets and records

The [HTML studies](../reference/studies-v1.html) and [approved prototype](../reference/approved-prototype.html) run offline and are references, not the shipped app. The [icon source package](../artwork/kitchen-timer-icon/README.md) includes the generated concept and editable vectors. Public screenshots are in `docs/images/`; temporary authoring tools and raw diagnostic evidence are intentionally excluded from Git.
