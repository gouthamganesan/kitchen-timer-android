package dev.goutham.kitchentimer.haptics

import dev.goutham.kitchentimer.model.HapticEvent
import dev.goutham.kitchentimer.model.MAX_TIMER_MINUTES

/** A source must be supplied even inside a gesture: state/render updates are never input. */
enum class DialChangeOrigin { USER_SETTING, COUNTDOWN, RESTORE, PRESET, PROGRAMMATIC }

/**
 * Selects feedback for deliberate winding. It has no Android dependency, scheduler or replay queue.
 * Call begin after drag slop (or before one accessibility adjustment), then onMove and end.
 * requestedMinutes is the unclamped input; passing it through exposes an attempted physical stop.
 */
class DialHapticPolicy(
    private val maxMinutes: Int = MAX_TIMER_MINUTES,
    private val minimumIntervalMillis: Long = 40L,
) {
    init {
        require(maxMinutes > 0)
        require(minimumIntervalMillis >= 0)
    }

    private var active = false
    private var moved = false
    private var latchedBoundary: Int? = null
    private var lastEmissionMillis: Long? = null

    fun begin(startMinutes: Int) {
        active = true
        moved = false
        val start = startMinutes.coerceIn(0, maxMinutes)
        if (start != latchedBoundary) latchedBoundary = null
    }

    fun onMove(
        previousMinutes: Int,
        requestedMinutes: Int,
        origin: DialChangeOrigin,
        eventTimeMillis: Long,
    ): HapticEvent? {
        if (!active || origin != DialChangeOrigin.USER_SETTING) return null
        val previous = previousMinutes.coerceIn(0, maxMinutes)
        val current = requestedMinutes.coerceIn(0, maxMinutes)
        if (requestedMinutes == previous) return null
        moved = true

        val boundary = when (current) {
            0 -> 0
            maxMinutes -> maxMinutes
            else -> null
        }
        val newBoundary = boundary != null && latchedBoundary != boundary
        // Latch even a rate-limited encounter. Holding a stop must never fire a delayed buzz.
        latchedBoundary = boundary

        val event = when {
            // Positive hour layers change at 60, 120, etc. in either direction.
            previous / 60 != current / 60 -> HapticEvent.DIAL_HOUR
            newBoundary -> HapticEvent.DIAL_BOUNDARY
            previous == current -> return null
            crossesFiveMinuteMark(previous, current) -> HapticEvent.DIAL_FIVE_MINUTES
            else -> HapticEvent.DIAL_MINUTE
        }
        val last = lastEmissionMillis
        if (last != null && eventTimeMillis - last < minimumIntervalMillis) return null
        lastEmissionMillis = eventTimeMillis
        return event
    }

    private fun crossesFiveMinuteMark(previous: Int, current: Int): Boolean =
        if (current > previous) previous / 5 != current / 5
        else (previous + 4) / 5 != (current + 4) / 5

    /** A tap/cancellation does not get a release tick; a boundary push is a real adjustment. */
    fun end(cancelled: Boolean = false): HapticEvent? {
        val settle = active && moved && !cancelled
        active = false
        moved = false
        return if (settle) HapticEvent.DIAL_RELEASE else null
    }
}
