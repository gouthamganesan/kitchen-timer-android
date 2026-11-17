package dev.goutham.kitchentimer.platform

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.goutham.kitchentimer.MainActivity
import dev.goutham.kitchentimer.R
import dev.goutham.kitchentimer.core.TimerRecord
import dev.goutham.kitchentimer.model.*
import kotlin.math.*

internal class TimerNotifications(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    init { channels() }
    private fun channels() {
        manager.createNotificationChannel(NotificationChannel(COUNTDOWN_CHANNEL, "Timer countdown", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Running and paused timer controls"; setSound(null, null); enableVibration(false)
        })
        manager.createNotificationChannel(NotificationChannel(ALARM_CHANNEL, "Timer finished", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Alarm sound or vibration until you stop the timer"
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), ALARM_AUDIO)
            enableVibration(true)
        })
    }
    fun post(record: TimerRecord, settings: TimerSettings) {
        if (record.phase == TimerPhase.READY) { manager.cancel(ID); return }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        manager.notify(ID, build(record, settings))
    }
    fun build(record: TimerRecord, settings: TimerSettings): Notification {
        val ringing = record.phase == TimerPhase.RINGING
        val running = record.phase == TimerPhase.RUNNING
        val left = if (running) (record.elapsedDeadline - SystemClock.elapsedRealtime()).coerceAtLeast(0) else record.remaining
        val status = when (record.phase) { TimerPhase.RUNNING -> "Timer running"; TimerPhase.PAUSED -> "Timer paused"; TimerPhase.RINGING -> "Time is up"; else -> "Timer" }
        val builder = NotificationCompat.Builder(context, if (ringing) ALARM_CHANNEL else COUNTDOWN_CHANNEL)
            .setSmallIcon(R.drawable.ic_timer).setContentTitle(status).setContentText(formatTime(left))
            .setContentIntent(openIntent()).setOngoing(true).setOnlyAlertOnce(true)
            .setCategory(if (ringing) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setShowWhen(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(content(R.layout.notification_compact, record, status, left))
            .setCustomBigContentView(content(R.layout.notification_expanded, record, status, left))
            // Repeating output is owned by the service; suppress the one-shot system alert.
            .setSilent(true)
        if (ringing) builder.addAction(R.drawable.ic_timer, "Stop", actionIntent("stop", record.generation))
        else {
            builder.addAction(R.drawable.ic_timer, if (running) "Pause" else "Resume", actionIntent(if (running) "pause" else "resume", record.generation))
            builder.addAction(R.drawable.ic_timer, "Reset", actionIntent("reset", record.generation))
        }
        return builder.build()
    }
    private fun content(layout: Int, record: TimerRecord, status: String, left: Long) = RemoteViews(context.packageName, layout).apply {
        setImageViewBitmap(R.id.notification_dial, dialBitmap(left))
        setTextViewText(R.id.notification_status, status)
        val running = record.phase == TimerPhase.RUNNING
        setViewVisibility(R.id.notification_chronometer, if (running) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.notification_time, if (running) View.GONE else View.VISIBLE)
        setTextViewText(R.id.notification_time, if (record.phase == TimerPhase.RINGING) "00:00" else formatTime(left))
        setChronometer(R.id.notification_chronometer, record.elapsedDeadline, null, running)
        setChronometerCountDown(R.id.notification_chronometer, true)
    }
    private fun openIntent() = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun actionIntent(action: String, generation: Long) = PendingIntent.getBroadcast(context, 2,
        Intent(context, TimerActionReceiver::class.java).setAction(action).setData(Uri.parse("timer://action/$action/$generation"))
            .putExtra(TimerScheduler.EXTRA_GENERATION, generation), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun dialBitmap(millis: Long): Bitmap {
        val bitmap = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, 240f, 240f, intArrayOf(Color.rgb(86, 94, 78), Color.rgb(24, 30, 25)), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(0f, 0f, 240f, 240f, 40f, 40f, paint)
        paint.shader = null; paint.color = Color.rgb(238, 236, 218)
        canvas.drawRoundRect(12f, 12f, 228f, 228f, 29f, 29f, paint)
        val minutes = millis / 60_000.0
        val hours = floor(minutes / 60).toInt()
        fun red(layer: Int): Int { val dark = (1.0 - layer * 0.065).coerceAtLeast(0.36); return Color.rgb((196 * dark).toInt(), (60 * dark).toInt(), (43 * dark).toInt()) }
        if (hours > 0) { paint.color = red(hours - 1); canvas.drawCircle(120f, 120f, 72f, paint) }
        val remainder = minutes % 60
        if (remainder > 0) { paint.color = red(hours); canvas.drawArc(48f,48f,192f,192f,-90f,(remainder * 6).toFloat(),true,paint) }
        paint.color = Color.rgb(53, 61, 47)
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0 - 90); val r1 = if (i % 5 == 0) 87 else 92
            paint.strokeWidth = if (i % 5 == 0) 2.6f else 1f
            canvas.drawLine((120 + cos(a)*r1).toFloat(),(120+sin(a)*r1).toFloat(),(120+cos(a)*99).toFloat(),(120+sin(a)*99).toFloat(),paint)
        }
        paint.color = Color.rgb(174, 173, 151); canvas.drawCircle(122f,124f,19f,paint)
        paint.shader = LinearGradient(100f,100f,140f,140f,Color.WHITE,Color.rgb(204,205,180),Shader.TileMode.CLAMP)
        canvas.drawCircle(120f,120f,18f,paint)
        return bitmap
    }
    companion object {
        const val ID = 25
        const val COUNTDOWN_CHANNEL = "timer_countdown"
        const val ALARM_CHANNEL = "timer_alarm"
        val ALARM_AUDIO: AudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        fun formatTime(millis: Long): String {
            val seconds = (millis.coerceAtLeast(0) + 999) / 1000
            return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
            else "%02d:%02d".format(seconds / 60, seconds % 60)
        }
    }
}
