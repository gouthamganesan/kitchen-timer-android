package dev.goutham.kitchentimer.core

import dev.goutham.kitchentimer.model.*

/** All deadlines are monotonic within one boot. Wall time is only a reboot bridge. */
data class ClockReading(val elapsed: Long, val wall: Long, val boot: Int)
data class TimerRecord(
    val phase: TimerPhase = TimerPhase.READY,
    val selected: Long = DEFAULT_TIMER_MILLIS,
    val remaining: Long = DEFAULT_TIMER_MILLIS,
    val elapsedDeadline: Long = 0,
    val wallDeadline: Long = 0,
    val boot: Int = -1,
    val generation: Long = 1,
) {
    fun remainingAt(now: ClockReading): Long = if (phase != TimerPhase.RUNNING) remaining
        else ((if (boot == now.boot) elapsedDeadline - now.elapsed else wallDeadline - now.wall)).coerceIn(0, selected)
    fun snapshot(now: ClockReading) = TimerSnapshot(selected, remainingAt(now), phase)
}

object TimerEngine {
    fun setDuration(old: TimerRecord, minutes: Int): TimerRecord {
        val duration = minutes.coerceIn(0, MAX_TIMER_MINUTES) * 60_000L
        return TimerRecord(selected = duration, remaining = duration, generation = old.generation + 1)
    }
    fun start(old: TimerRecord, now: ClockReading): TimerRecord {
        if (old.phase !in listOf(TimerPhase.READY, TimerPhase.PAUSED) || old.remaining <= 0) return old
        return old.copy(phase = TimerPhase.RUNNING, elapsedDeadline = now.elapsed + old.remaining,
            wallDeadline = now.wall + old.remaining, boot = now.boot, generation = old.generation + 1)
    }
    fun pause(old: TimerRecord, now: ClockReading): TimerRecord {
        if (old.phase != TimerPhase.RUNNING) return old
        val left = old.remainingAt(now)
        if (left == 0L) return reconcile(old, now)
        return old.copy(phase = TimerPhase.PAUSED, remaining = left, elapsedDeadline = 0,
            wallDeadline = 0, generation = old.generation + 1)
    }
    fun reset(old: TimerRecord) = TimerRecord(selected = 0, remaining = 0, generation = old.generation + 1)
    fun stopAlarm(old: TimerRecord) = TimerRecord(selected = old.selected, remaining = old.selected, generation = old.generation + 1)
    fun reconcile(old: TimerRecord, now: ClockReading): TimerRecord {
        if (old.phase != TimerPhase.RUNNING) return old
        val left = old.remainingAt(now)
        if (left <= 0) return old.copy(phase = TimerPhase.RINGING, remaining = 0, elapsedDeadline = 0, wallDeadline = 0)
        // Rebase after reboot; update the wall bridge after wall-clock edits without changing elapsed deadline.
        return old.copy(remaining = left, elapsedDeadline = now.elapsed + left, wallDeadline = now.wall + left, boot = now.boot)
    }
    fun expire(old: TimerRecord, deliveredGeneration: Long, now: ClockReading): TimerRecord =
        if (old.phase == TimerPhase.RUNNING && old.generation == deliveredGeneration) reconcile(old, now) else old
}
