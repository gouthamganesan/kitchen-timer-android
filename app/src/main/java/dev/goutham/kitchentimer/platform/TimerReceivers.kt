package dev.goutham.kitchentimer.platform

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TimerRepository.get(context).onAlarm(intent.getLongExtra(TimerScheduler.EXTRA_GENERATION, -1))
    }
}
class TimerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TimerRepository.get(context).onNotificationAction(intent.action, intent.getLongExtra(TimerScheduler.EXTRA_GENERATION, -1))
    }
}
class TimerRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> TimerRepository.get(context).refresh()
        }
    }
}
