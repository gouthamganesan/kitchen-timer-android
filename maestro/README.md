# Device flows

Run one flow at a time. Root/integration owns the device session. These flows are authored against agreed UI semantics and must be checked against the actual first build before being called PASS. They preserve app data, but deliberately reset the test timer and change theme/focus settings. Do not run while a real timer is in use.

Environment:

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
# Run from the repository root
~/.maestro/bin/maestro test --udid DEVICE_SERIAL maestro/01-controls.yaml
```

Grant notification and exact-alarm access through the app/system UI before any flow, including the first three. Run 01 controls, 02 focus/themes, 03 duration picker first. Then 05 notification actions and 04 background alarm. Flow 04 uses a real one-minute timer and may sound; stop must silence it immediately. Maestro reports and screenshots are retained under its test output; copy evidence into artifacts/ and record paths in docs/testing/TEST-MATRIX.md. A screenshot alone does not prove timing or physical feel.

If a test cannot locate a control, inspect with Maestro hierarchy first. Prefer semantic selectors; do not hide a failed assertion with optional taps or replace the flow with shell coordinate input. Focus mode must expose no Quick set action. The common baseline uses the checked state of the Focus mode switch to restore regular mode; verify this selector once against actual Compose semantics.

Manual/device-specific checks live in docs/testing/DEVICE-CHECKLIST.md. They cover denied/revoked permissions, physical haptics, rotary trajectories, lock/Doze, process death, reboot, clock changes, OS theme, font scale and notification expansion. The two notification flows use a Maestro swipe from the status-bar edge because installed Maestro 2.7.0 has no openNotifications command. Native notification layouts and system permission labels can vary by Android/OEM and need device-specific flows after inspection.
