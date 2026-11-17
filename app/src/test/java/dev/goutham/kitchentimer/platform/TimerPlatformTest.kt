package dev.goutham.kitchentimer.platform

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.VibratorManager
import android.media.AudioManager
import android.media.MediaPlayer
import dev.goutham.kitchentimer.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import org.robolectric.util.ReflectionHelpers
import org.robolectric.shadows.ShadowSystemClock
import org.robolectric.shadows.ShadowMediaPlayer
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TimerPlatformTest {
    private lateinit var app: Application
    @Before fun setup() {
        app = RuntimeEnvironment.getApplication()
        // Robolectric reuses the app-class sandbox across methods; each case needs its own application singleton.
        ReflectionHelpers.setStaticField(TimerRepository::class.java, "instance", null)
        app.getSharedPreferences("timer_state", Context.MODE_PRIVATE).edit().clear().commit()
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
    }
    @Test fun pauseCancelsSystemAlarmAndRejectsStaleDelivery() {
        val repo = TimerRepository.get(app)
        repo.dispatch(TimerAction.SetDuration(1)); repo.dispatch(TimerAction.ToggleRun)
        val generation = repo.currentRecord().generation
        val alarms = shadowOf(app.getSystemService(AlarmManager::class.java))
        assertEquals(1, alarms.scheduledAlarms.size)
        repo.dispatch(TimerAction.ToggleRun)
        assertTrue(alarms.scheduledAlarms.isEmpty())
        repo.onAlarm(generation)
        assertEquals(TimerPhase.PAUSED, repo.state.value.timer.phase)
    }
    @Test fun resetCancelsAndLateNotificationResumeCannotRestart() {
        val repo = TimerRepository.get(app)
        repo.dispatch(TimerAction.ToggleRun); repo.dispatch(TimerAction.ToggleRun)
        val staleGeneration = repo.currentRecord().generation
        repo.dispatch(TimerAction.Reset)
        repo.onNotificationAction("resume", staleGeneration)
        assertEquals(TimerPhase.READY, repo.state.value.timer.phase)
        assertEquals(0L, repo.state.value.timer.remainingMillis)
        assertTrue(shadowOf(app.getSystemService(AlarmManager::class.java)).scheduledAlarms.isEmpty())
    }
    @Test fun noExactAccessRefusesStart() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val repo = TimerRepository.get(app)
        repo.dispatch(TimerAction.ToggleRun)
        assertEquals(TimerPhase.READY, repo.state.value.timer.phase)
        assertTrue(shadowOf(app.getSystemService(AlarmManager::class.java)).scheduledAlarms.isEmpty())
    }
    @Test fun duplicateExpiryStartsOnlyOneRingingService() {
        val repo = TimerRepository.get(app)
        repo.dispatch(TimerAction.SetDuration(1)); repo.dispatch(TimerAction.ToggleRun)
        val generation = repo.currentRecord().generation
        ShadowSystemClock.advanceBy(Duration.ofMinutes(2))
        repo.onAlarm(generation)
        assertEquals(TimerPhase.RINGING, repo.state.value.timer.phase)
        assertNotNull(shadowOf(app).nextStartedService)
        repo.onAlarm(generation)
        assertNull(shadowOf(app).nextStartedService)
    }
    @Test fun latePauseNotificationDoesNotStopAnExpiredAlarm() {
        val repo = TimerRepository.get(app)
        repo.dispatch(TimerAction.SetDuration(1)); repo.dispatch(TimerAction.ToggleRun)
        val generation = repo.currentRecord().generation
        ShadowSystemClock.advanceBy(Duration.ofMinutes(2))
        repo.onNotificationAction("pause", generation)
        assertEquals(TimerPhase.RINGING, repo.state.value.timer.phase)
    }
    @Test fun ringingVibrationStopsWithServiceCleanup() {
        seedRinging()
        val controller = Robolectric.buildService(TimerAlarmService::class.java).create()
        val service = controller.get()
        service.onStartCommand(Intent().putExtra(TimerScheduler.EXTRA_GENERATION, 7L), 0, 1)
        val vibration = shadowOf(app.getSystemService(VibratorManager::class.java).defaultVibrator)
        assertTrue(vibration.isVibrating)
        TimerRepository.get(app).dispatch(TimerAction.StopAlarm)
        controller.destroy()
        assertTrue(vibration.isCancelled)
        assertFalse(vibration.isVibrating)
        assertEquals(TimerPhase.READY, TimerRepository.get(app).state.value.timer.phase)
    }
    @Test fun foregroundPermissionRaceStopsServiceWithoutCrashing() {
        seedRinging()
        val controller = Robolectric.buildService(TimerAlarmService::class.java).create()
        val service = controller.get()
        shadowOf(service).setThrowInStartForeground(SecurityException("revoked"))
        service.onStartCommand(Intent().putExtra(TimerScheduler.EXTRA_GENERATION, 7L), 0, 1)
        assertTrue(shadowOf(service).isStoppedBySelf)
        assertFalse(shadowOf(app.getSystemService(VibratorManager::class.java).defaultVibrator).isVibrating)
        controller.destroy()
    }
    @Test fun staleServiceLaunchDoesNotStartOutput() {
        seedRinging()
        val controller = Robolectric.buildService(TimerAlarmService::class.java).create()
        val service = controller.get()
        service.onStartCommand(Intent().putExtra(TimerScheduler.EXTRA_GENERATION, 6L), 0, 1)
        assertTrue(shadowOf(service).isStoppedBySelf)
        assertFalse(shadowOf(app.getSystemService(VibratorManager::class.java).defaultVibrator).isVibrating)
        controller.destroy()
    }
    @Test fun asynchronousSoundFailureReleasesPlayerAndAudioFocus() {
        seedRinging()
        app.getSharedPreferences("timer_state", Context.MODE_PRIVATE).edit().putString("alarm_mode", "SOUND").commit()
        ShadowMediaPlayer.setMediaInfoProvider { ShadowMediaPlayer.MediaInfo(1000, 0) }
        var media: MediaPlayer? = null
        ShadowMediaPlayer.setCreateListener { created, _ -> media = created }
        val controller = Robolectric.buildService(TimerAlarmService::class.java).create()
        controller.get().onStartCommand(Intent().putExtra(TimerScheduler.EXTRA_GENERATION, 7L), 0, 1)
        assertNotNull(media)
        val playerShadow = shadowOf(media!!)
        playerShadow.invokeErrorListener(MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        assertEquals(ShadowMediaPlayer.State.END, playerShadow.state)
        assertNotNull(shadowOf(app.getSystemService(AudioManager::class.java)).lastAbandonedAudioFocusRequest)
        assertEquals(TimerPhase.RINGING, TimerRepository.get(app).state.value.timer.phase)
        controller.destroy()
    }
    private fun seedRinging() {
        app.getSharedPreferences("timer_state", Context.MODE_PRIVATE).edit()
            .putString("phase", "RINGING").putLong("selected", 60_000).putLong("remaining", 0)
            .putLong("generation", 7).putString("alarm_mode", "VIBRATE").commit()
    }
}
