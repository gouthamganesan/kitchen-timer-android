package dev.goutham.kitchentimer.core

import dev.goutham.kitchentimer.model.*
import org.junit.Assert.*
import org.junit.Test

class TimerEngineTest {
    private val now = ClockReading(10_000, 1_000_000, 9)
    @Test fun countdownUsesElapsedClockDespiteWallJump() {
        val running = TimerEngine.start(TimerEngine.setDuration(TimerRecord(), 5), now)
        assertEquals(240_000, running.remainingAt(now.copy(elapsed = 70_000, wall = 9_000_000)))
    }
    @Test fun pauseAndResumePreserveExactRemaining() {
        val running = TimerEngine.start(TimerRecord(), now)
        val paused = TimerEngine.pause(running, now.copy(elapsed = 10_123))
        assertEquals(1_499_877, paused.remaining)
        val resumed = TimerEngine.start(paused, now.copy(elapsed = 20_000))
        assertEquals(1_519_877, resumed.elapsedDeadline)
        assertTrue(resumed.generation > paused.generation)
    }
    @Test fun previousAlarmCannotRingReplacementOrPausedTimer() {
        val running = TimerEngine.start(TimerRecord(), now)
        val later = now.copy(elapsed = 99_999_999)
        val replacement = TimerEngine.setDuration(running, 2)
        assertEquals(replacement, TimerEngine.expire(replacement, running.generation, later))
        val paused = TimerEngine.pause(running, now.copy(elapsed = 11_000))
        assertEquals(paused, TimerEngine.expire(paused, running.generation, later))
    }
    @Test fun expiryIsIdempotentAndRetainsSelectedDurationForStop() {
        val running = TimerEngine.start(TimerRecord(), now)
        val expired = TimerEngine.expire(running, running.generation, now.copy(elapsed = running.elapsedDeadline))
        assertEquals(TimerPhase.RINGING, expired.phase)
        assertEquals(expired, TimerEngine.expire(expired, running.generation, now))
        assertEquals(DEFAULT_TIMER_MILLIS, TimerEngine.stopAlarm(expired).remaining)
    }
    @Test fun rebootUsesWallDeadlineAndRebasesMonotonicDeadline() {
        val running = TimerEngine.start(TimerEngine.setDuration(TimerRecord(), 5), now)
        val reboot = ClockReading(5_000, now.wall + 120_000, 10)
        val restored = TimerEngine.reconcile(running, reboot)
        assertEquals(180_000, restored.remaining)
        assertEquals(185_000, restored.elapsedDeadline)
        assertEquals(10, restored.boot)
    }
    @Test fun rebootAfterDeadlineRings() {
        val running = TimerEngine.start(TimerEngine.setDuration(TimerRecord(), 1), now)
        assertEquals(TimerPhase.RINGING, TimerEngine.reconcile(running, ClockReading(100, now.wall + 90_000, 10)).phase)
    }
    @Test fun clockEditBridgeDoesNotShiftRemaining() {
        val running = TimerEngine.start(TimerRecord(), now)
        val changed = now.copy(elapsed = now.elapsed + 10_000, wall = now.wall - 600_000)
        val refreshed = TimerEngine.reconcile(running, changed)
        assertEquals(running.elapsedDeadline, refreshed.elapsedDeadline)
        assertEquals(changed.wall + 1_490_000, refreshed.wallDeadline)
    }
    @Test fun boundsAndZeroCannotStart() {
        val zero = TimerEngine.setDuration(TimerRecord(), -9)
        assertEquals(zero, TimerEngine.start(zero, now))
        assertEquals(MAX_TIMER_MINUTES * 60_000L, TimerEngine.setDuration(zero, 9_999).remaining)
        assertEquals(0, TimerEngine.reset(TimerRecord()).remaining)
    }
    @Test fun resetInvalidatesAnyPendingAlarm() {
        val running = TimerEngine.start(TimerRecord(), now)
        val reset = TimerEngine.reset(running)
        assertEquals(reset, TimerEngine.expire(reset, running.generation, now.copy(elapsed = Long.MAX_VALUE)))
    }
}
