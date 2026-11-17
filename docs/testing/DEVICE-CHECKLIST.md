# Android device verification

Record device model, Android/API version, app version/APK checksum, time, tester and evidence directory for every run. Emulator and physical-phone results are separate. Root/integration owns device automation; coordinate before attaching another session. The user intends to connect a phone for E2E testing.

## Before starting

Install the debug APK without clearing an existing user's data. Confirm no real timer is in use. Record notification/exact-alarm access, channel controls and touch-feedback settings before changing them. The automated suite resets its timer and adjusts theme/focus settings; restore the user's choices afterward. Use Maestro for taps, typing, gestures and assertions; adb is suitable for installation, logs and diagnostics.

Run the semantic flows in maestro/README.md. If a selector fails, inspect the actual hierarchy and correct the flow or accessibility implementation, then rerun the affected case. Do not mark a UI test passed from syntax checking alone.

## Physical feel

| Case | Action and acceptance |
| --- | --- |
| Tiny / firm tick | Wind slowly across individual minutes and five-minute marks in both directions. Minute is subtle, five-minute arrival firmer. No two effects for one movement. |
| Hour layer | Wind 59→60 and 60→59. Distinct clunk follows the full-layer change; reverse the winding and repeat across 120. |
| Stops | Reach zero and continue trying below it. One stop, then silence while held. Move inside and return for one fresh stop. Repeat at 23h59m. |
| Fast winding | Spin several revolutions quickly. No accumulating/replayed buzz train after the finger stops; verify the final duration/layers remain accurate. |
| Passive silence | Set 61 minutes, start, keep the phone in hand while crossing the 60-minute layer and minute marks. There must be no dial ticks. Shorter test: set 6 minutes and cross 5. This long check is not replaced by emulator silence. |
| Programmatic assignment | Choose a preset or confirm picker. One selection confirmation is allowed; never a series of minute/five/hour ticks. Restore/reopen a running timer with no touch feedback. |
| Plungers | Press start/pause/resume and reset. Feedback aligns with cap movement and lit icon; release feels softer. Drag off/cancel should not trigger action. |
| Sheets / picker | Pull settings past the catch threshold, settle, dismiss. No repeated feedback from animation frames. Picker tick and Set confirmation must work while dialog owns window focus. |
| Focus | Centre single tap acts immediately. Double tap does not expose Quick set. No hidden Quick set accessibility action. |
| System setting | Turn touch feedback off in Android settings and retry all controls. UI stays usable and silent. Restore preference afterward. Alarm vibration remains a separately selected alarm function. |
| Completion | Select vibration alarm, start one minute, lock/background. Grouped buzz repeats until Stop. Stop immediately cuts it off; there is no leaked repeating vibration after reset or mode switch. Repeat in sound mode, checking channel/audio/DND behaviour. |

The platform tunes action constants to each motor. Relative intent is testable, identical amplitude/duration across phone models is not promised. Devices unable to produce suitably subtle frequent ticks may intentionally omit them.

## Time, lifecycle and permissions

| Case | Action and acceptance |
| --- | --- |
| Background / lock | Set 1 minute, start, Home, lock screen. Expiry occurs at deadline and exposes Stop. Record actual start/expiry times and notification screenshot. Repeat with another app in foreground. |
| Notification actions | Running notification Pause freezes remaining time; Resume continues from it; Reset cancels the pending expiration. Ringing Stop silences and restores selected time. Inspect compact and expanded forms. |
| Stale delivery | Start 1 minute, pause/reset or replace at ~50 seconds. Wait beyond original deadline. No old alarm. Start replacement and verify only its own deadline fires once. |
| Process death | Put app in background and use system-style process kill (not force-stop). Reopen before expiry and after expiry; remaining time and single ring delivery recover correctly. Capture repository/AlarmManager diagnostics. |
| Force-stop | Android force-stop cancels app scheduling. Document this limitation; relaunch recovery must be honest. Do not present a force-stop failure as ordinary background expiry. |
| Reboot | Use a dedicated emulator or explicitly scheduled physical test: start a timer long enough to reboot. Recover/reschedule after boot with wall deadline; past-due recovery should be consistent and never duplicate. Do not unexpectedly reboot the user's phone. |
| Clock / timezone | While running, adjust wall clock/timezone on dedicated test device; same-boot monotonic remaining duration stays stable. Restore original settings. |
| Notification denied | Deny notifications on API33+. Start attempt shows actionable explanation, no crash or false claim of visible persistent notification. Grant through app flow and retry. |
| Exact access | Where required by the chosen alarm permission model, deny/revoke exact alarm access and observe actionable state; restoration reschedules safely. No silent imprecise fallback presented as exact. |
| DND / channel | Restrict alarm notification channel and use DND. Verify the app respects OS controls and does not claim to bypass them. Restore controls. |
| Visible-only awake | Enable Stay awake, leave app visible beyond device timeout; it remains awake. Home/lock/background removes its screen flag. Reopen and preference persists. Disable and check ordinary timeout. |

## Appearance and accessibility

Capture regular Light, regular Dark, focus Light and focus Dark. Regular dark keeps casing/red material unchanged with yellow-green LCD edge origin and warm-white dial edge diffusion. Focus keeps the old minimal face and never exposes presets.

Exercise Light/Dark/Follow system in both focus states, including an actual OS theme change. Explicit theme stays pinned; System changes immediately. Relaunch and confirm stored theme/focus/sound/awake choices.

Test portrait, landscape, compact split screen where available, font scale 1.3/2.0 and disabled animations. Reach every setting and Stop without clipping. TalkBack should name icon actions and announce duration without speaking every second. Dial adjustment must have an accessibility alternative. Keyboard manual duration accepts 0–23 hours and 0–59 minutes, rejects invalid/empty values clearly, and Cancel/Back discards draft. Check hours inner ring, exact minute picking and no label overlap.
