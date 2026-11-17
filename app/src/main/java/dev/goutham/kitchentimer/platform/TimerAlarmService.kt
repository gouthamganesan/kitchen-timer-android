package dev.goutham.kitchentimer.platform

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.*
import android.os.*
import android.net.Uri
import dev.goutham.kitchentimer.model.*

/** Exists only while the alarm is ringing, never throughout a countdown. */
class TimerAlarmService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var focusRequest: AudioFocusRequest? = null
    private var activeGeneration: Long? = null
    private var activeMode: AlarmMode? = null
    private lateinit var repository: TimerRepository
    private lateinit var notifications: TimerNotifications
    private val audio by lazy { getSystemService(AudioManager::class.java) }
    override fun onCreate() {
        super.onCreate()
        repository = TimerRepository.get(this)
        notifications = TimerNotifications(this)
        vibrator = if (Build.VERSION.SDK_INT >= 31) getSystemService(VibratorManager::class.java).defaultVibrator
            else @Suppress("DEPRECATION") (getSystemService(VIBRATOR_SERVICE) as Vibrator)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val record = repository.currentRecord()
        val generation = intent?.getLongExtra(TimerScheduler.EXTRA_GENERATION, -1) ?: record.generation
        if (record.phase != TimerPhase.RINGING || generation != record.generation) {
            if (activeGeneration == null || record.phase != TimerPhase.RINGING) stopSelf(startId)
            return START_NOT_STICKY
        }
        if (!TimerScheduler(this).available()) { stopSelf(startId); return START_NOT_STICKY }
        val notification = notifications.build(record, repository.state.value.settings)
        try {
            if (Build.VERSION.SDK_INT >= 34) startForeground(TimerNotifications.ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED)
            else startForeground(TimerNotifications.ID, notification)
        } catch (_: SecurityException) {
            // Exact access can be revoked after launch but before the service becomes foreground.
            stopOutput(); stopSelf(startId); return START_NOT_STICKY
        } catch (_: IllegalStateException) {
            stopOutput(); stopSelf(startId); return START_NOT_STICKY
        }
        val mode = repository.state.value.settings.alarmMode
        if (activeGeneration != record.generation || activeMode != mode) {
            activeGeneration = record.generation
            activeMode = mode
            play(mode)
        }
        return START_STICKY
    }
    private fun play(mode: AlarmMode) {
        stopOutput()
        val channel = getSystemService(NotificationManager::class.java).getNotificationChannel(TimerNotifications.ALARM_CHANNEL)
        // Honor the user disabling this channel or its sound/vibration in Android settings.
        if (channel.importance == NotificationManager.IMPORTANCE_NONE) return
        if (mode == AlarmMode.VIBRATE) {
            if (!channel.shouldVibrate()) return
            val effect = VibrationEffect.createWaveform(longArrayOf(0, 450, 220, 450, 220, 450, 1100), 0)
            if (Build.VERSION.SDK_INT >= 33) vibrator?.vibrate(effect, VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build())
            else @Suppress("DEPRECATION") vibrator?.vibrate(effect, TimerNotifications.ALARM_AUDIO)
            return
        }
        val uri = channel.sound ?: return
        startSound(uri, allowFallback = true)
    }
    private fun startSound(uri: Uri, allowFallback: Boolean) {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(TimerNotifications.ALARM_AUDIO)
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_GAIN -> runCatching { player?.start() }
                    AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> runCatching { player?.pause() }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.setVolume(0.2f, 0.2f)
                }
                if (change == AudioManager.AUDIOFOCUS_GAIN) player?.setVolume(1f, 1f)
            }.build()
        focusRequest = request
        if (audio.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        val media = MediaPlayer()
        player = media
        try {
            media.setAudioAttributes(TimerNotifications.ALARM_AUDIO)
            media.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
            media.setDataSource(this, uri)
            media.isLooping = true
            media.setOnPreparedListener { it.start() }
            media.setOnErrorListener { failed, _, _ ->
                if (player === failed) handlePlaybackFailure(uri, allowFallback)
                true
            }
            media.prepareAsync()
        } catch (_: Exception) {
            handlePlaybackFailure(uri, allowFallback)
        }
    }
    private fun handlePlaybackFailure(uri: Uri, allowFallback: Boolean) {
        releaseSound()
        val generation = activeGeneration
        val fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (allowFallback && fallback != null && fallback != uri) {
            Handler(Looper.getMainLooper()).post {
                val current = repository.currentRecord()
                if (current.phase == TimerPhase.RINGING && current.generation == generation &&
                    activeGeneration == generation && repository.state.value.settings.alarmMode == AlarmMode.SOUND) {
                    startSound(fallback, allowFallback = false)
                }
            }
        } else {
            // Keep Stop visible, but release resources and allow a future explicit recovery to retry.
            activeMode = null
        }
    }
    private fun releaseSound() {
        player?.let { runCatching { it.stop() }; it.release() }; player = null
        focusRequest?.let { audio.abandonAudioFocusRequest(it) }; focusRequest = null
    }
    private fun stopOutput() {
        releaseSound()
        vibrator?.cancel()
    }
    override fun onDestroy() {
        stopOutput()
        // Remove only a still-ringing notification. A pause/new countdown may already have replaced it.
        if (repository.state.value.timer.phase == TimerPhase.RINGING) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(STOP_FOREGROUND_DETACH)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?) = null
}
