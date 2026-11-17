# Test matrix

Updated 2026-11-17. Results distinguish automated logic checks, emulator observations and physical-device validation. Raw device logs, identifiers and machine-specific reports remain local; reviewed screenshots are published in [images](../images).

| Area | Result | Scope and limits |
| --- | --- | --- |
| Build and lint | PASS locally, 0.1.2 | APK compiles; zero lint errors. Advisory dependency/style warnings remain. |
| Core timer | 9 unit tests PASS | [TimerEngineTest](../../app/src/test/java/dev/goutham/kitchentimer/core/TimerEngineTest.kt): deadline/state and clock/boot simulation. |
| Haptic policy | 13 unit tests PASS | [DialHapticPolicyTest](../../app/src/test/java/dev/goutham/kitchentimer/haptics/DialHapticPolicyTest.kt): explicit input, boundaries, priority and rate limiting. |
| Android platform | 9 unit tests PASS | [TimerPlatformTest](../../app/src/test/java/dev/goutham/kitchentimer/platform/TimerPlatformTest.kt): alarms, stale actions and service cleanup. |
| Controls and focus | PASS on emulator | Baseline 0.1.1: start/pause/resume/reset, fixed presets, no focus presets and focus single-tap behavior. |
| Picker | PARTIAL | Numeric commit/cancel and compact Cancel passed. Full circular exact-picking, invalid-to-clock and cancellation matrix remains. |
| Winding | PASS on emulator | Two exact full turns reached 2:00:00 without Settings interception after stable LCD geometry fix. Second pointers/cancellation/bounds need fuller checks. |
| Compact layout | PASS on emulator | Baseline 0.1.1 at 320×568dp, font scale 1.3: portrait, picker, focus, full landscape rocker and Settings. |
| Latest visual refinement | PASS on emulator, 0.1.2 | Sound rocker toggled both ways; normal/backlit/focus themes, picker and Settings captured. Source removes the row's Material indication; no target/layout changes. |
| Notification actions | PASS in recovery flow | Expanded notification Pause/Resume/Reset passed. The full formal 05 flow was revised after a collapsed-notification failure and still needs a fresh complete rerun. |
| Background expiry | PASS on emulator | Real one-minute countdown in background, alarm notification, Stop and return to READY. |
| Process-death recovery | PASS on emulator | System event log confirmed process death, receiver restart at expiry and successful Stop. Not force-stop or reboot. |
| Phone installation | PASS, 0.1.2 | Installed over USB and package version verified; existing app data retained. Installation is not a physical-feel test. |
| Physical haptics/audio | PENDING | Motor strength, speaker output, DND, touch feedback off, locked screen, Doze and OEM behavior. |
| Permissions and reboot | PARTIAL | Selected logic tests pass; complete denial/revocation UI, real reboot and wall-time sequences remain. |
| Accessibility and settings | PARTIAL | Semantic controls exercised. Full TalkBack, OS-follow theme changes, disabled animations, Stay awake lifecycle and persistence need dedicated runtime checks. |

CI runs the same build/unit/lint tasks. Consult the repository's Actions runs for actual hosted results; local success is not a claim that hosted CI has run. Use [Maestro instructions](../../maestro/README.md) and the [device checklist](DEVICE-CHECKLIST.md) for further checks.
