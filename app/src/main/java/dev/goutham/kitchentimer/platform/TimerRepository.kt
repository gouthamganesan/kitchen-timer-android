package dev.goutham.kitchentimer.platform

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.ContextCompat
import dev.goutham.kitchentimer.core.*
import dev.goutham.kitchentimer.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Application-scoped authoritative state. UI ticker only paints; AlarmManager owns background expiry. */
class TimerRepository private constructor(private val context: Context) {
    private val store = TimerStore(context)
    private val scheduler = TimerScheduler(context)
    private val notifications = TimerNotifications(context)
    private var record = store.loadRecord()
    private var settings = store.loadSettings()
    private val mutableState = MutableStateFlow(TimerAppState(record.snapshot(clock()), settings))
    val state: StateFlow<TimerAppState> = mutableState.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    init {
        scope.launch {
            mutableState.subscriptionCount.map { it > 0 }.distinctUntilChanged().collectLatest { observed ->
                if (observed) while (isActive) { tick(); delay(200) }
            }
        }
    }
    private fun clock() = ClockReading(SystemClock.elapsedRealtime(), System.currentTimeMillis(),
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1))

    @Synchronized fun dispatch(action: TimerAction) {
        val now = clock()
        val old = record
        val oldSettings = settings
        record = when (action) {
            is TimerAction.SetDuration -> TimerEngine.setDuration(record, action.minutes)
            TimerAction.BeginSetting -> when (record.phase) {
                TimerPhase.RUNNING -> TimerEngine.pause(record, now)
                TimerPhase.RINGING -> TimerEngine.stopAlarm(record)
                else -> record
            }
            TimerAction.ToggleRun -> when (record.phase) {
                TimerPhase.RUNNING -> TimerEngine.pause(record, now)
                TimerPhase.RINGING -> TimerEngine.stopAlarm(record)
                else -> if (scheduler.available()) TimerEngine.start(record, now) else record
            }
            TimerAction.Reset -> TimerEngine.reset(record)
            TimerAction.StopAlarm -> if (record.phase == TimerPhase.RINGING) TimerEngine.stopAlarm(record) else record
            else -> record
        }
        settings = when (action) {
            is TimerAction.SetStayAwake -> settings.copy(stayAwake = action.enabled)
            is TimerAction.SetFocusMode -> settings.copy(focusMode = action.enabled)
            is TimerAction.SetTheme -> settings.copy(themeMode = action.theme)
            is TimerAction.SetAlarmMode -> settings.copy(alarmMode = action.mode)
            else -> settings
        }
        if (record != old || settings != oldSettings) commit(old)
        else publish()
    }

    /** Activity resume and trusted boot/time/permission recovery broadcasts call this. */
    @Synchronized fun refresh() {
        val old = record
        record = TimerEngine.reconcile(record, clock())
        if (record.phase == TimerPhase.RUNNING && !scheduler.available()) {
            record = TimerEngine.pause(record, clock())
        }
        commit(old)
    }

    @Synchronized internal fun onAlarm(generation: Long) {
        if (record.phase != TimerPhase.RUNNING || record.generation != generation) return
        val old = record
        record = TimerEngine.expire(record, generation, clock())
        commit(old)
    }

    @Synchronized internal fun onNotificationAction(action: String?, generation: Long) {
        if (record.generation != generation) return
        // Refresh before interpreting actions, so a late Pause cannot dismiss an already expired alarm.
        val before = record
        record = TimerEngine.reconcile(record, clock())
        if (record != before) commit(before)
        when (action) {
            "pause" -> if (record.phase == TimerPhase.RUNNING) dispatch(TimerAction.ToggleRun)
            "resume" -> if (record.phase == TimerPhase.PAUSED) dispatch(TimerAction.ToggleRun)
            "reset" -> dispatch(TimerAction.Reset)
            "stop" -> dispatch(TimerAction.StopAlarm)
        }
    }

    @Synchronized internal fun currentRecord() = record
    @Synchronized private fun tick() {
        if (record.phase == TimerPhase.RUNNING && record.remainingAt(clock()) == 0L) {
            val old = record
            record = TimerEngine.reconcile(record, clock())
            commit(old)
        } else publish()
    }
    private fun publish() { mutableState.value = TimerAppState(record.snapshot(clock()), settings) }
    private fun commit(old: TimerRecord) {
        store.save(record, settings)
        if (old.generation != record.generation || record.phase != TimerPhase.RUNNING) scheduler.cancel(old.generation)
        if (record.phase == TimerPhase.RUNNING && !scheduler.schedule(record)) {
            val running = record
            record = TimerEngine.pause(record, clock())
            store.save(record, settings)
            scheduler.cancel(running.generation)
        }
        publish()
        when (record.phase) {
            TimerPhase.RINGING -> {
                notifications.post(record, settings)
                val intent = Intent(context, TimerAlarmService::class.java).putExtra(TimerScheduler.EXTRA_GENERATION, record.generation)
                try { ContextCompat.startForegroundService(context, intent) }
                catch (_: IllegalStateException) { /* A visible alarm notification remains if the OS forbids this recovery start. */ }
                catch (_: SecurityException) { /* Access may have been revoked between check and service launch. */ }
            }
            else -> {
                context.stopService(Intent(context, TimerAlarmService::class.java))
                notifications.post(record, settings)
            }
        }
    }
    companion object {
        // Constructor is private; get() always converts callers to applicationContext, never an Activity.
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: TimerRepository? = null
        fun get(context: Context): TimerRepository = instance ?: synchronized(this) {
            instance ?: TimerRepository(context.applicationContext).also { instance = it }
        }
    }
}
