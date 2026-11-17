package dev.goutham.kitchentimer.platform

import android.content.Context
import dev.goutham.kitchentimer.core.TimerRecord
import dev.goutham.kitchentimer.model.*

internal class TimerStore(context: Context) {
    private val prefs = context.getSharedPreferences("timer_state", Context.MODE_PRIVATE)
    fun loadRecord() = TimerRecord(
        phase = enumValue(prefs.getString("phase", null), TimerPhase.READY),
        selected = prefs.getLong("selected", DEFAULT_TIMER_MILLIS).coerceIn(0, MAX_TIMER_MINUTES * 60_000L),
        remaining = prefs.getLong("remaining", DEFAULT_TIMER_MILLIS).coerceIn(0, MAX_TIMER_MINUTES * 60_000L),
        elapsedDeadline = prefs.getLong("elapsed_deadline", 0), wallDeadline = prefs.getLong("wall_deadline", 0),
        boot = prefs.getInt("boot", -1), generation = prefs.getLong("generation", 1),
    )
    fun loadSettings() = TimerSettings(
        stayAwake = prefs.getBoolean("stay_awake", false), focusMode = prefs.getBoolean("focus", false),
        themeMode = enumValue(prefs.getString("theme", null), ThemeMode.SYSTEM),
        alarmMode = enumValue(prefs.getString("alarm_mode", null), AlarmMode.SOUND),
    )
    fun save(record: TimerRecord, settings: TimerSettings) {
        // A single small synchronous transaction must finish before registering the external alarm.
        check(prefs.edit().putString("phase", record.phase.name).putLong("selected", record.selected)
            .putLong("remaining", record.remaining).putLong("elapsed_deadline", record.elapsedDeadline)
            .putLong("wall_deadline", record.wallDeadline).putInt("boot", record.boot)
            .putLong("generation", record.generation).putBoolean("stay_awake", settings.stayAwake)
            .putBoolean("focus", settings.focusMode).putString("theme", settings.themeMode.name)
            .putString("alarm_mode", settings.alarmMode.name).commit()) { "Could not save timer state" }
    }
    private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback
}
