# Architecture and ownership contract

Native Kotlin + Jetpack Compose/Canvas. Single app module, min 26 / target 36. Package dev.goutham.kitchentimer; app label Kitchen Timer. Use existing local Gradle 8.14.3, AGP 8.12.0, Kotlin/Compose plugin 2.3.21 unless a verified compatibility issue requires adjustment; existing Compose BOM 2025.06.01 is known locally. Build owns dependency decisions. No WebView or network permission.

## Shared contracts (integration agent owns)

`model/TimerContracts.kt` defines TimerPhase, AlarmMode, ThemeMode, TimerSnapshot, TimerSettings, TimerAppState, TimerCapabilities, TimerAction and HapticEvent. Do not change it without messaging integration. Duration units are milliseconds; SetDuration carries minutes. TimerSnapshot deadline details may remain internal to persistence.

UI must expose:
`@Composable fun TimerScreen(state: TimerAppState, capabilities: TimerCapabilities, onAction: (TimerAction) -> Unit, onHaptic: (HapticEvent) -> Unit)` in `ui/TimerScreen.kt`.

Platform must expose `platform/TimerRepository.kt` with `companion object fun get(context: Context): TimerRepository`, `val state: StateFlow<TimerAppState>`, `fun dispatch(action: TimerAction)`, `fun refresh()`. Repository is application-context singleton; authoritative timer/settings state shared by Activity, alarm receiver, notification receiver and alarm service. Activity observes state and handles RequestNotifications / RequestExactAlarms actions itself.

Haptics must expose `haptics/TimerHaptics.kt`: `class TimerHaptics(private val view: android.view.View) { fun perform(event: HapticEvent) }`. Provide a pure `DialHapticPolicy` helper plus meaningful tests; coordinate its signature directly with UI worker. Haptic policy has explicit user-setting origin, no countdown observation. Alarm vibration is owned by platform alarm service, separate from UI haptics.

## Ownership

- platform agent: Gradle wrapper/config; app AndroidManifest; core/, platform/; app resources except res/font and UI drawables; core tests. Owns alarm, persistence, notifications and permissions declarations. May add repository metadata methods only after coordination.
- ui agent: ui/ only plus UI-specific drawable assets under drawable/ui_*; docs/agents/ui-*; native appearance, gestures, picker, quick set, settings, a11y. Reads the approved HTML and Barlow font.
- haptics_qa agent: haptics/, haptics tests, maestro/, docs/testing/ and docs/agents/haptics-qa-*; device test flows and physical test instructions. Do not drive devices concurrently with integration.
- integration/root: shared model contract; MainActivity.kt, TimerApplication.kt if needed; top-level docs/README; app assembly, final build/lint/run coordination and evidence. Coordinates changes to ownership rather than reverting another agent.

## Reliability

Core deadline/state machine is pure and unit-testable. Same-boot elapsedRealtime prevents wall-clock adjustments altering remaining duration; persisted wall deadline plus boot evidence supports documented reboot recovery. Every schedule has generation/token identity; stale/duplicate alarms are ignored. Pause/reset/replacement cancel previous schedules. OS timers trigger expiry, UI ticker only paints. Countdown never calls haptic policy. Show permission limitations rather than silently using inaccurate background timing.

Alarm/FGS implementation must verify current official Android docs. Prefer no FGS throughout countdown. Exact alarm -> ringing service if required, foreground notification and cleanup. Do not label normal user-dismissable ongoing notification as guaranteed permanent. Reboot reschedules from persisted state.
