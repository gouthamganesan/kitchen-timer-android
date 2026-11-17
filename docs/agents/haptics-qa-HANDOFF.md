# Haptics / QA handoff

Status: implementation complete; root integration build, 31 tests/lint and several emulator flows pass as of 2026-11-17. Compact/font-scale clipping is a confirmed open issue under UI repair. Phone installed, physical feedback pending.

## Files and contracts

- `app/src/main/java/dev/goutham/kitchentimer/haptics/TimerHaptics.kt`: `TimerHaptics(view).perform(HapticEvent)`. Uses Android action constants, API-aware fallbacks, no ignore-user-setting flags and no raw vibration fallback. It checks attached/shown/window visibility, deliberately not window focus because Compose dialogs temporarily own focus. Root may pass Activity decorView.
- `haptics/DialHapticPolicy.kt`: pure setting policy with `begin(startMinutes)`, `onMove(previousMinutes, requestedMinutes, origin, eventTimeMillis)`, `end(cancelled=false)`. Pass the unclamped requested minute for hard stops, monotonic timestamp, and explicit `DialChangeOrigin.USER_SETTING`. Every non-user origin is suppressed. UI is integrated.
- `app/src/test/java/dev/goutham/kitchentimer/haptics/DialHapticPolicyTest.kt`: 13 independent tests.
- `maestro/01-controls.yaml`, `02-focus-themes.yaml`, `03-duration-picker.yaml`, `04-background-alarm.yaml`, `05-notification-actions.yaml`, plus `common/regular-ready.yaml`. See maestro/README.md for prerequisites.
- `docs/testing/TEST-MATRIX.md` and `DEVICE-CHECKLIST.md`: acceptance tracking and actual physical-device procedure.

## Policy details

Hour feedback follows full-hour layer changes in either direction; five-minute feedback happens on arrival at a mark (26→25), not departure (25→24). One strongest event per onMove call: hour, boundary, five, minute. A strict 40ms interval discards overly rapid events without replay; a discarded boundary encounter still latches, avoiding a delayed buzz while held. End-stop latches persist across gestures until user departs. Release settles only after an actual adjustment/boundary push; idle/cancelled gestures are silent. Alarm repeating vibration belongs to platform service and is intentionally separate.

## Checks actually run

- Isolated Kotlin 2.3.21/JUnit 4.13.2 compile and test execution: PASS, 13 tests. `artifacts/haptics-policy-tests/result.txt`.
- Local Maestro 2.7.0 `check-syntax`: PASS, six YAMLs. `artifacts/maestro-syntax/*.txt`. Initial unsupported openNotifications was fixed with Maestro notification-shade swipe.
- Read-only UI dispatch review: no rendering/countdown policy invocation found. UI adopted merged settings toggle semantics following QA feedback.
- Read-only platform review: concrete async MediaPlayer failure/focus leak reported to platform, acknowledged; platform owns fix and its validation.
- Root integration checks: verified-build.log (local evidence: `verified-build.log`) PASS; 31 tests, lint zero errors/20 warnings. Emulator controls/focus/themes/picker and one-minute background expiry+Stop PASS. Expanded-notification recovery actions PASS; revised complete notification flow still pending rerun. See [Evidence record](../testing/EVIDENCE-2026-11-17.md) for actual logs and limits.
- Compact 320×568 at 1.3 font scale FAIL (picker Cancel clipped); UI owner fixing. Connected Pixel 11 Pro installation succeeded, but no automated phone run or physical feel/sound/lock-screen result is recorded.
- Actual emulator process-death recovery now PASS in a later run: process-events.txt (local evidence: `process-events.txt`) confirms killed PID 6828 and new AlarmReceiver PID 9455; process-start.log (local evidence: `process-start.log`) and process-expiry.log (local evidence: `process-expiry.log`) pass scheduled expiry and Stop. Earlier failed kill remains invalid evidence; phone process death/reboot/force-stop remain untested.

## Reproduce and continue

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
# Run from the repository root
./gradlew :app:testDebugUnitTest --tests 'dev.goutham.kitchentimer.haptics.DialHapticPolicyTest'
~/.maestro/bin/maestro check-syntax maestro/01-controls.yaml
~/.maestro/bin/maestro --udid DEVICE_SERIAL test maestro/01-controls.yaml
```

Grant notification and exact-alarm access in app/system UI before all flows. Flows reset the test timer and change theme/focus but do not clear app data. Maestro syntax does not validate actual UI selector matching. Screenshots are useful for appearance; physical motor feel, channel/DND, locked delivery, Doze, OEM notification layout, clock/reboot and touch-feedback preferences need real-device checks. Do not run overlapping device sessions or unexpectedly reboot the user's phone.

## Known limits

Platform action constants intentionally leave motor tuning to Android. API34 frequent ticks can be omitted by hardware unable to produce a sufficiently gentle effect. Strong events may be suppressed during extreme winding by the strict rate cap. Preset/picker commits may emit one confirmation but never dial tick sequences. Notification flows may need inspected OEM-specific expansion selectors; use Maestro hierarchy before changing them. No app branding, publishing or outward hosting was performed.

## Follow-up UI review

Three actionable findings were reported read-only and fixed by UI owner: restored sheet's passive settle haptic; ringing-drag baseline mismatch; invalid keyboard draft trapped behind a clamped clock selection. See PROGRESS for exact trigger/fix descriptions. Root rebuilds and runs regressions; do not convert source-owner fix confirmation into a device PASS.


## Integration evidence update

The acceptance matrix and dated evidence record now distinguish successful emulator flows from the failed collapsed-notification first attempt, corrected recovery flow, pending full rerun and compact-layout failure. User manual phone testing is in progress; do not infer a result. Root owns the device and subsequent updates.


## Process-death evidence follow-up

Updated the test matrix/evidence record with a genuine OS-confirmed background kill and receiver restart; preserved the earlier ineffective adb attempt. Latest naming/icon and grip-to-screen-swipe UI changes postdate baseline evidence and need candidate rebuild plus affected-flow reruns. No device operation was performed by QA.
