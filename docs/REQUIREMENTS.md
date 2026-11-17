# Kitchen timer Android requirements

Status: approved conversation translated into build requirements, 2026-11-17. Source of truth priority: latest user corrections, this document, approved prototype. Product name: Kitchen Timer (user confirmed 2026-11-17). Build a runnable debug Android app and maintain documentation sufficient for fresh agents to continue implementation and physical-device E2E testing.

## 1. Product and delivery

R01 Native Android Kotlin/Compose application with buildable vector/Canvas hardware appearance. The main purpose is a kitchen countdown timer whose look, movement, lighting and feedback feel physical.
R02 Reuse the approved red-disc design, not the old multi-concept gallery. The app contains no prototype headings, phone mock frame/status-bar imitation, comparison chips, platform explanations, or notification-demo gallery. Respect real Android status/navigation insets.
R03 One local timer. No accounts, cloud, ads, analytics, tracking, network permission or published backend. Store settings and recoverable timer state locally.
R04 Provide reproducible Gradle build, debug APK, focused unit tests, Maestro flows, documented test evidence and explicit remaining physical-device checks. No release/store publication.
R05 Implementation assumptions: minSdk 26, compile/targetSdk 36, portrait optimized but usable with landscape/large-font layouts. Duration 0–23h59m, one-minute setting granularity; countdown displays seconds. These are inherited prototype assumptions, not branding/product commitments.

## 2. Approved visual specification

V01 Regular view: charcoal rubber/plastic housing fills the app content; inset green-gray LCD above a square ivory analog face; large red remaining-time disc; printed dark marks and 5-minute numbers clockwise 60/5/10/.../55; small raised ivory centre knob; physical shadows and consistent upper-left ambient lighting.
V02 Remove titles, READY TO WIND/status copy and separate full-hour counter. The exact LCD duration is authoritative, including hours when necessary.
V03 Every completed hour fills the base in a progressively darker red shade. Partial next hour uses the next darker shade, beginning at 12 o'clock clockwise. At exact full-hour boundaries no accidental partial wedge. At zero disc is empty. Multi-hour layers must work both in setting and countdown without rendering-triggered haptics.
V04 Two physical circular plungers: reset-return-arrow icon and play/pause/stop icon reflecting current action. No printed START/RESUME/RESET labels. Accessible labels remain precise. Caps depress, retain sidewalls/sockets, and illuminate on press with a short decay; reset lamp yellow-green, primary lamp warm ivory/amber. Haptics synchronize with press/release, not frame rendering.
V05 Sound/vibration hardware rocker uses speaker/phone-vibration icons. Sound mode rings audibly; vibration mode uses repeating grouped buzzes. Stop remains available until acknowledged. Interaction haptics remain separate from alarm mode.
V06 Remove the app-drawn horizontal dash below the speaker grille. An upward swipe on the screen opens settings; it must not hijack rotary dial winding. Keep an accessibility/keyboard settings action. System Back closes the top sheet/dialog first.

## 3. Theme, focus and settings

S01 Settings sheet includes Stay awake, Focus mode and Theme. Theme is a GENERAL setting independent of Focus mode: Light / Dark / Follow system. Follow system is default; explicit choice persists and remains pinned across system changes. Follow system responds immediately to OS changes.
S02 Stay awake affects the visible activity only and clears on exit/background; it is not a permanent device setting or a background CPU wake lock.
S03 Regular Light: original unlit green-gray LCD, ivory dial, charcoal casing.
S04 Regular Dark: keep casing, red disc and normal physical surfaces intact. Only illuminate LCD and analog timer face. LCD light is yellow-green, strongest at an edge-emitter origin and fading across the LCD; dark digits block light like an actual reflective LCD. Analog face uses soft whitish-yellow diffusion and localized edge pools. Knob has a warm rim reflection. Avoid uniform neon halos or wholesale palette inversion.
S05 Focus Light: preserve earlier minimal full ivory surface with red disc, printed scale and centre knob. Hide LCD, physical buttons, sound rocker, grille and decorative chrome. Keep upward-swipe and accessibility settings access reachable.
S06 Focus Dark: preserve earlier minimal deep charcoal/teal surface, amber layered disc, pale scale and green-gray knob. This is intentionally different from regular mode's localized backlight.
S07 Focus centre single tap starts/pauses/resumes. Quick presets are not available in Focus mode, by any pointer, keyboard or accessibility path. Do not introduce double-tap latency to the Focus single-tap action.

## 4. Timer behaviour and input

T01 READY, RUNNING, PAUSED, RINGING states. Play starts/resumes; pause captures remaining duration; reset cancels alarm/notification and sets zero; zero cannot start. Stop alarm stops sound/vibration immediately and restores last selected duration ready.
T02 Clockwise winding increases minutes; reverse decreases. Multiple revolutions add hours; crossing 12 o'clock must not jump/wrap duration. Clamp at zero/max. Correctly track pointer cancellation, second pointers and leaving bounds. Beginning time editing while running pauses/cancels the old schedule before replacement; no stale alarm may subsequently ring.
T03 Dragging the centre may wind; distinguish taps from drags. No duration changes from incidental taps on the dial.
T04 LCD tap opens a custom Material-style duration picker matching the timer palette. Circular hours and minutes, hour inner ring for 12–23, exact minutes 0–59, auto-advance from hours to minutes, selected values legible without label collisions. Keyboard numeric alternative, clear bounds/errors, cancel/back discards draft. Duration has no AM/PM. Committing sets READY and does not auto-start.
T05 Regular centre-knob double tap opens Quick set with fixed 1, 3, 5, 10, 15, 20, 30 and 60-minute buttons. No customization/editing. Selection sets READY, without auto-start. Opening/cancelling must not pause or change a running timer; only committed selection replaces it. Provide accessible activation as alternative to the gesture. No quick set in Focus mode.
T06 Timekeeping uses monotonic deadlines while in the same boot, not decrement-per-callback counting. Persist enough state to recover after process death; account for clock changes and reboot. Reopening after deadline must transition exactly once to ringing/expired according to documented recovery semantics.
T07 Background/locked-screen completion must be scheduled with AlarmManager and a suitable PendingIntent, never only a Compose coroutine or Handler. Verify exact-alarm access and surface actionable permission failure. Handle permission loss, stale deliveries, duplicate receivers, reset/pause races, reboot/package update rescheduling. Android force-stop limitations must be documented, not concealed.
T08 Persist focus/theme/stay-awake preference and alarm mode. Countdown changes must not cause preference rewrites every frame. Safe local storage, minimal exported Android components, immutable PendingIntents where applicable.

## 5. Haptic map

H01 SETTING ONLY: minute crossing = tiny tick; five-minute crossing = firmer tick; complete hour = distinct heavier clunk coordinated with colour-layer change; zero/max = firm end-stop once per boundary encounter. These NEVER happen while passively counting down, restoring state, applying presets, updating notification/drawables, or background transitions. A drag editing a running timer first transitions to setting.
H02 Priority hour > boundary/five-minute > minute; do not stack effects for one crossing. Rate-limit rapid winding so a fast turn does not enqueue a buzz train. Deduplicate an end-stop until the user moves away and reaches it again.
H03 Dial release = subtle settling click after an actual setting drag, not an idle tap.
H04 Start/pause/resume = crisp press and softer release synchronized with cap movement/light; reset = deeper click; rocker = short decisive snap.
H05 Regular quick-set opening = small double-tap acknowledgement; preset selection = one clean confirmation. Manual picker values = light ticks; Set = firmer confirmation. Avoid duplicate platform keyboard feedback.
H06 Settings sheet = small catch at opening threshold, soft settle at expanded/collapsed state; settings switch/theme selection = gentle click only when value changes.
H07 Completion vibration alarm = repeating grouped buzzes until stopped. Stop cuts it off immediately with brief confirmation where suitable. No countdown-second haptics, automatic theme/backlight haptics, or background interaction haptics.
H08 Prefer Android action constants / supported primitives with capability-aware fallback and system touch-feedback preferences. Physical feel requires real-device review; emulator checks prove dispatch and gating only. Never claim identical haptic strength across hardware.

## 6. Notifications and alarm delivery

N01 Ongoing notification while running/paused with exact remaining countdown and meaningful state, open-app intent, Pause/Resume and Reset actions. Alarm notification exposes Stop. Actions update the same authoritative timer state as the app, including when app UI is absent.
N02 Honor Android 12+ system decoration and supported custom RemoteViews sizes. Keep app icon, system header and standard labeled actions; app's own push buttons are icons, native notification action labels remain appropriate. Compact legible countdown and small timer illustration; expanded illustration/LCD-style content where supported. Use native Chronometer for background countdown, frozen text while paused.
N03 The clock must remain accurate without posting every second. Thumbnail is a snapshot on published updates; native countdown text is authoritative in background. Document this native constraint. Custom-content notification is not a promoted Live Update.
N04 Notification permission on Android 13+ must be explained/requested in context. Respect system channels, sound/vibration/DND controls. Ongoing cannot be promised permanently non-dismissible on modern Android.
N05 Repeating alarm delivery uses a documented, legal foreground-service type where required; user-visible notification, audio focus/attributes and lifecycle cleanup. No foreground service solely to hold a CPU loop for countdown. No full-screen intent unless justified and reviewed. Use AlarmManager rather than WorkManager for precise expiration.

## 7. Accessibility and quality

A01 Minimum 48dp intended touch targets, semantic labels for icon buttons and dial value, keyboard/accessibility alternatives to gestures, status announcements that do not speak every second.
A02 Respect font scale and reduced/disabled animation where relevant. Contrast survives lit/unlit appearances. Sheet content scrolls on compact/landscape screens. No clipping or unavailable Stop/Back/settings actions.
A03 Re-rendering, lifecycle resume and passive clock ticks must never invoke haptic callbacks. No vibration from Compose effects keyed to remaining time.
A04 Clean dependency footprint, debug logs without sensitive data, no leaked wake locks/audio/vibration/jobs, no battery drain from busy loops.

## 8. Acceptance and evidence

Build debug APK + unit tests + lint. Verify regular Light/Dark, both Focus themes, global System theme, settings persistence, rotary wraparound/hour layers, zero/max, reset/start/pause/resume, picker/cancel/invalid values, presets regular-only, press lights, haptic gating, permissions, notification actions, background expiry, process death, reboot recovery and stop cleanup. Use the test matrix in docs/testing/TEST-MATRIX.md and record PASS/FAIL/NOT RUN per environment. Real phone: physical haptics, audio/vibration, lock screen/Doze, OEM battery controls and notification presentation. Preserve screenshots/log evidence in artifacts/ and reference it from progress/handoff docs.

## 9. App identity (user update, 2026-11-17)

I01 App label is Kitchen Timer. Create a distinctive, meaningful physical kitchen-timer icon, first generated as a raster concept then rebuilt as editable SVG. Deliver the source image, vector, previews and native launcher resources; preserve the approved charcoal/ivory/red-disc identity.
