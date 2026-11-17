package dev.goutham.kitchentimer.model

const val MAX_TIMER_MINUTES = 23 * 60 + 59
const val DEFAULT_TIMER_MILLIS = 25L * 60_000L

enum class TimerPhase { READY, RUNNING, PAUSED, RINGING }
enum class AlarmMode { SOUND, VIBRATE }
enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class TimerSnapshot(
    val selectedDurationMillis: Long = DEFAULT_TIMER_MILLIS,
    val remainingMillis: Long = DEFAULT_TIMER_MILLIS,
    val phase: TimerPhase = TimerPhase.READY,
)
data class TimerSettings(
    val stayAwake: Boolean = false,
    val focusMode: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val alarmMode: AlarmMode = AlarmMode.SOUND,
)
data class TimerAppState(val timer: TimerSnapshot = TimerSnapshot(), val settings: TimerSettings = TimerSettings())
data class TimerCapabilities(val notificationsGranted: Boolean = true, val exactAlarmsGranted: Boolean = true)

sealed interface TimerAction {
    data class SetDuration(val minutes: Int) : TimerAction
    data object BeginSetting : TimerAction
    data object ToggleRun : TimerAction
    data object Reset : TimerAction
    data object StopAlarm : TimerAction
    data class SetStayAwake(val enabled: Boolean) : TimerAction
    data class SetFocusMode(val enabled: Boolean) : TimerAction
    data class SetTheme(val theme: ThemeMode) : TimerAction
    data class SetAlarmMode(val mode: AlarmMode) : TimerAction
    data object RequestNotifications : TimerAction
    data object RequestExactAlarms : TimerAction
}

enum class HapticEvent {
    DIAL_MINUTE, DIAL_FIVE_MINUTES, DIAL_HOUR, DIAL_BOUNDARY, DIAL_RELEASE,
    BUTTON_PRESS, BUTTON_RELEASE, RESET, ROCKER_SWITCH, QUICK_OPEN, PRESET_SELECTED,
    PICKER_TICK, PICKER_CONFIRM, SHEET_THRESHOLD, SHEET_SETTLE, SETTINGS_SELECT, ALARM_STOP,
}
