# Haptics / QA progress

2026-11-17 16:40 IST: Read requirements, architecture and ownership. Confirmed official Android API34 segment constants support minute/selection semantics and respect platform implementation. Agreed proposed begin/onMove/end policy with UI; implementation starting.

Checks: not yet run. Device operations are root-owned. Physical feel, audio, lock screen and OEM notification presentation remain NOT RUN.

Next: pure policy and native adapter, then tests and semantic Maestro flows.

2026-11-17: Implemented DialHapticPolicy, TimerHaptics and 13 unit cases. Root review caught dialog focus gating and descending five-minute arrival; both corrected with regression coverage. Independent Kotlin 2.3.21/JUnit4.13.2 compilation/run PASS (13 tests), evidence artifacts/haptics-policy-tests/result.txt. Full Gradle suite remains integration-owned and pending complete app sources.

Authored five semantic Maestro flows plus common baseline and device checklist. Syntax checking is running without touching devices. Flows and physical checks are not yet executed.

2026-11-17: All 6 Maestro YAML files pass local Maestro 2.7.0 check-syntax. The initial openNotifications commands failed syntax and were replaced with a Maestro status-bar-edge swipe; corrected checks pass. Evidence: artifacts/maestro-syntax/*.txt. No UI flow has been run by this agent. Updated README: all flows require prior notification and exact-alarm access.

Source review: UI invokes dial policy only from deliberate gesture/accessibility adjustments; no countdown/render haptic call found. Suggested one merged toggle row for settings rather than duplicate label/switch targets; UI implemented it. Root-requested platform review found async MediaPlayer failure retaining audio focus and a silent ringing state; reported concrete issue to platform, which acknowledged and owns fix. Monotonic/generation/recovery logic showed no other high-confidence correctness issue in reviewed paths.

Next: root runs build/full tests/lint and Maestro on emulator; physical E2E awaits connected phone. Physical feel and permission/lock/OEM behaviours remain unverified.

2026-11-17, follow-up read-only UI review requested by root: reviewed TimerScreen/TimerDialogs for winding, focus, draft handling and haptic origin. Sent three concrete findings to UI and root without modifying UI: (1) restoring an open settings sheet after configuration recreation emits a passive settle haptic; gate with unsaved user-open origin. (2) dragging a ringing timer captures zero accumulator then BeginSetting restores selected duration, causing a visible baseline jump before first whole-minute adjustment; align initial state/accumulator. (3) invalid keyboard draft 24h/99m renders clamped 23h/59m in clock mode but tapping displayed bound is ignored, leaving Set disabled; route intentional selection through raw draft comparison. UI owns fixes. Normal unwrap/multiple revolutions, opposite-direction clamp release, focus immediate/no-quick guards, cancelled draft isolation and no countdown dial-policy call were coherent in inspected source. Physical/multi-pointer behaviour still needs device validation.

2026-11-17: UI agent reports all three follow-up findings fixed: unsaved explicit-open token gates sheet settle on actual expansion; ringing edits consistently start from selected duration across pointer/keyboard/accessibility; clock choices always reach raw-draft comparison so invalid numeric values can be replaced by displayed valid bounds. Root is rebuilding. Fixes have source-owner confirmation; emulator/phone regression validation is still pending.
