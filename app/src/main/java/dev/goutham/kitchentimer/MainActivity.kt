package dev.goutham.kitchentimer

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.goutham.kitchentimer.haptics.TimerHaptics
import dev.goutham.kitchentimer.model.ThemeMode
import dev.goutham.kitchentimer.model.TimerAction
import dev.goutham.kitchentimer.model.TimerCapabilities
import dev.goutham.kitchentimer.model.TimerPhase
import dev.goutham.kitchentimer.platform.TimerRepository
import dev.goutham.kitchentimer.ui.TimerScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository by lazy { TimerRepository.get(applicationContext) }
    private val haptics by lazy { TimerHaptics(window.decorView) }
    private var capabilities by mutableStateOf(TimerCapabilities())
    private var visible = false
    private var startAfterNotificationRequest = false
    private val permissionHistory by lazy { getSharedPreferences("activity_permissions", MODE_PRIVATE) }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshCapabilities()
        if (startAfterNotificationRequest) {
            startAfterNotificationRequest = false
            startIfAllowed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshCapabilities()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.state.collect { state -> updateScreenAwake(state.settings.stayAwake) }
            }
        }
        setContent {
            val state by repository.state.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val dark = when (state.settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }
            val darkSystemIcons = state.settings.focusMode && !dark
            SideEffect {
                val style = if (darkSystemIcons) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    else SystemBarStyle.dark(Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                TimerScreen(state, capabilities, ::handleAction, haptics::perform)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        visible = true
        updateScreenAwake(repository.state.value.settings.stayAwake)
    }

    override fun onResume() {
        super.onResume()
        refreshCapabilities()
        repository.refresh()
    }

    override fun onStop() {
        visible = false
        updateScreenAwake(false)
        super.onStop()
    }

    private fun updateScreenAwake(enabled: Boolean) {
        if (visible && enabled) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun refreshCapabilities() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        capabilities = TimerCapabilities(
            notificationsGranted = NotificationManagerCompat.from(this).areNotificationsEnabled(),
            exactAlarmsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms(),
        )
    }

    private fun handleAction(action: TimerAction) {
        when (action) {
            TimerAction.RequestNotifications -> requestNotifications(false)
            TimerAction.RequestExactAlarms -> requestExactAlarms()
            TimerAction.ToggleRun -> {
                val timer = repository.state.value.timer
                if (timer.phase == TimerPhase.RUNNING || timer.phase == TimerPhase.RINGING) {
                    repository.dispatch(action)
                } else if (timer.remainingMillis > 0) {
                    refreshCapabilities()
                    when {
                        !capabilities.exactAlarmsGranted -> requestExactAlarms()
                        needsNotificationPrompt() -> requestNotifications(true)
                        else -> startIfAllowed()
                    }
                }
            }
            else -> repository.dispatch(action)
        }
    }

    private fun startIfAllowed() {
        refreshCapabilities()
        val timer = repository.state.value.timer
        if (capabilities.exactAlarmsGranted && timer.remainingMillis > 0 &&
            (timer.phase == TimerPhase.READY || timer.phase == TimerPhase.PAUSED)) {
            repository.dispatch(TimerAction.ToggleRun)
        }
    }

    private fun needsNotificationPrompt(): Boolean = Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
        !permissionHistory.getBoolean("notification_requested", false)

    private fun requestNotifications(startAfter: Boolean) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            (!permissionHistory.getBoolean("notification_requested", false) || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))) {
            startAfterNotificationRequest = startAfter
            permissionHistory.edit().putBoolean("notification_requested", true).apply()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
        }
    }

    private fun requestExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Toast.makeText(this, "Allow alarms and reminders, then press play to start.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        }
    }
}
