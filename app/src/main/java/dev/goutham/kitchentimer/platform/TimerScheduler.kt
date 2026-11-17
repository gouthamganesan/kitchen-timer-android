package dev.goutham.kitchentimer.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dev.goutham.kitchentimer.core.TimerRecord

internal class TimerScheduler(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    fun available() = Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    fun schedule(record: TimerRecord): Boolean {
        if (!available()) return false
        return try {
            manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, record.elapsedDeadline, intent(record.generation))
            true
        } catch (_: SecurityException) { false }
    }
    fun cancel(generation: Long) { val pending = intent(generation); manager.cancel(pending); pending.cancel() }
    private fun intent(generation: Long): PendingIntent = PendingIntent.getBroadcast(context, 1,
        Intent(context, TimerAlarmReceiver::class.java).setData(Uri.parse("timer://expiry/$generation"))
            .putExtra(EXTRA_GENERATION, generation), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    companion object { const val EXTRA_GENERATION = "generation" }
}
