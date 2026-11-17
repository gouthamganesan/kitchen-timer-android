# Platform handoff

## Delivered

Native Gradle 8.14.3 / AGP 8.12.0 / Kotlin 2.3.21 / Compose BOM 2025.06.01 app configuration. minSdk 26, target/compile 36, Java 17. No Internet permission. Current label: Kitchen Timer. The bundled Gradle wrapper is checked against the official distribution checksum.

- `core/TimerEngine.kt`: pure monotonic deadline/state engine, reboot wall-time bridge, generation-safe expiration.
- `platform/TimerStore.kt`: single durable local state/settings transaction before external scheduling.
- `platform/TimerScheduler.kt`: checked `setExactAndAllowWhileIdle(ELAPSED_REALTIME_WAKEUP)` with immutable generation-specific PendingIntents.
- `platform/TimerRepository.kt`: agreed singleton StateFlow/dispatch/refresh API. Only observed UI gets 200ms painting ticks; alarm service does not subscribe or keep this ticker alive. No countdown FGS.
- `platform/TimerReceivers.kt`: expiration, stale-safe native actions and reboot/package/time/exact-access recovery.
- `platform/TimerNotifications.kt` and `res/layout/notification_*`: system decorated compact/expanded native RemoteViews, live Chronometer, static disc snapshot, labeled Pause/Resume/Reset/Stop actions.
- `platform/TimerAlarmService.kt`: foreground only while ringing; looped alarm audio or grouped vibration, Android channel controls, audio focus, and cleanup. Stale launches and foreground eligibility races stop safely. Asynchronous media failures release player/audio focus and make at most one generation-checked default-alarm fallback per playback attempt.
- `app/src/test/.../core/TimerEngineTest.kt`: 9 deadline/state cases.
- `app/src/test/.../platform/TimerPlatformTest.kt`: 9 Android alarm/action/service regression cases, including asynchronous media failure cleanup.

## Integration

Activity calls `TimerRepository.get(context)`, observes `state` lifecycle-aware, dispatches non-permission actions and calls `refresh()` on resume. Activity owns notification/exact permission prompts and visible-only stay-awake. Missing exact-alarm access refuses start in both Activity and repository. If recovered running state loses exact access, refresh pauses it.

Service type is **systemExempted**, which Android documents for exact-alarm permission holders continuing alarms, including vibration-only alarms. Manifest requests `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE_SYSTEM_EXEMPTED`, notifications, reboot, vibration and media wake lock. No full-screen intent, no permanent countdown service, no Internet.

Shared model contract was not modified. Root owns Activity/model/top-level docs. UI/haptic files were not edited.

## Checks

Initial build reached Kotlin and was blocked by unfinished UI. Integration subsequently compiled the APK. Initial full tests found 2 platform failures caused by Robolectric reusing the application singleton across methods; tests now reset the singleton for each fresh test application. Focused `:app:testDebugUnitTest --tests dev.goutham.kitchentimer.platform.TimerPlatformTest` then PASSED all 8 cases. Integration then built the APK and passed all 31 unit tests, including the ninth platform media-error regression. Lint found an API26 theme issue; platform fixes are ready for the next integration lint run. No device operation was performed by platform agent.

Run from project root with `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain`.

## Semantics and limits

- Same boot: changing wall time never changes remaining duration; TIME_SET updates the persisted wall bridge.
- After reboot: recover remaining from the wall deadline. If already expired, transition to ringing. If wall time itself changed while powered off, no app can reconstruct the lost monotonic clock; recovered remaining is bounded to the selected duration.
- Process death: OS scheduled alarm survives. Receiver loads persisted generation and deadline; duplicate/stale delivery cannot ring a replacement.
- Android force-stop cancels alarms and suppresses receivers until user opens the app. Opening reconciles persisted state. This cannot be guaranteed through force-stop.
- Ongoing notifications may be dismissible on modern Android. Exact alarm remains scheduled. POST_NOTIFICATIONS denial hides normal drawer content but does not remove the scheduled timer; foreground alarm still follows Android service rules.
- Notification artwork is a snapshot at state changes. Native Chronometer supplies the continuously accurate countdown without per-second reposting.
- Audio uses alarm stream/DND behavior and channel sound; vibration uses alarm usage and channel vibration. A disabled alarm channel intentionally disables service output.
- Emulator/unit tests prove behavior and dispatch, not physical haptic quality or speaker loudness. Real-phone lock screen/Doze/OEM behavior, channel presentation, audio focus and permission revocation remain device checks.

## Official references (verified 2026-11-17)

- https://developer.android.com/develop/background-work/services/alarms
- https://developer.android.com/develop/background-work/services/fgs/service-types
- https://developer.android.com/reference/android/content/pm/ServiceInfo#FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
- https://developer.android.com/about/versions/14/changes/schedule-exact-alarms

## Lint follow-up

Removed API27-only navigation-bar attribute from the API26 base theme; Activity already sets bar appearance compatibly. Recovery receiver now rejects unexpected actions. Android12 cloud/device-transfer exclusions explicitly keep local countdown state from being restored onto another device. The singleton Context lint warning is suppressed only at its private applicationContext-only field with the invariant documented. No dependency upgrades. KTX and newer-version suggestions remain nonblocking; UI window-size and haptic SDK warnings routed to their owners.

Backup syntax verified: https://developer.android.com/identity/data/autobackup
