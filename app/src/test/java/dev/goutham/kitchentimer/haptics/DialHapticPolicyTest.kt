package dev.goutham.kitchentimer.haptics

import dev.goutham.kitchentimer.model.HapticEvent
import dev.goutham.kitchentimer.model.MAX_TIMER_MINUTES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DialHapticPolicyTest {
    private val user = DialChangeOrigin.USER_SETTING

    @Test fun passiveSourcesNeverTickEvenDuringAnActiveGesture() {
        for (origin in DialChangeOrigin.entries.filter { it != user }) {
            val policy = DialHapticPolicy()
            policy.begin(120)
            for ((before, after) in listOf(120 to 119, 60 to 59, 5 to 4, 1 to 0, 0 to MAX_TIMER_MINUTES)) {
                assertNull("Unexpected feedback for $origin", policy.onMove(before, after, origin, 100L))
            }
            assertNull(policy.end())
        }
    }

    @Test fun noGestureNoTick() {
        assertNull(DialHapticPolicy().onMove(59, 60, user, 0L))
    }

    @Test fun eachStepChoosesOnlyItsStrongestCrossing() {
        val policy = DialHapticPolicy()
        policy.begin(3)
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(3, 4, user, 0L))
        assertEquals(HapticEvent.DIAL_FIVE_MINUTES, policy.onMove(4, 5, user, 40L))
        assertEquals(HapticEvent.DIAL_HOUR, policy.onMove(5, 61, user, 80L))
        assertEquals(HapticEvent.DIAL_HOUR, policy.onMove(61, 59, user, 120L))
        assertEquals(HapticEvent.DIAL_FIVE_MINUTES, policy.onMove(6, 5, user, 160L))
    }

    @Test fun descendingFiveMinuteTickOccursAtArrivalNotDeparture() {
        val policy = DialHapticPolicy()
        policy.begin(26)
        assertEquals(HapticEvent.DIAL_FIVE_MINUTES, policy.onMove(26, 25, user, 0L))
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(25, 24, user, 40L))
        assertEquals(HapticEvent.DIAL_FIVE_MINUTES, policy.onMove(24, 19, user, 80L))
    }

    @Test fun exactHourAndReverseLayerChangeClunk() {
        val policy = DialHapticPolicy()
        policy.begin(59)
        assertEquals(HapticEvent.DIAL_HOUR, policy.onMove(59, 60, user, 0L))
        assertEquals(HapticEvent.DIAL_HOUR, policy.onMove(60, 59, user, 40L))
    }

    @Test fun zeroStopLatchesAcrossGesturesUntilUserMovesAway() {
        val policy = DialHapticPolicy()
        policy.begin(1)
        assertEquals(HapticEvent.DIAL_BOUNDARY, policy.onMove(1, 0, user, 0L))
        assertNull(policy.onMove(0, -8, user, 100L))
        policy.end()
        policy.begin(0)
        assertNull(policy.onMove(0, -1, user, 200L))
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(0, 1, user, 300L))
        assertEquals(HapticEvent.DIAL_BOUNDARY, policy.onMove(1, -1, user, 400L))
    }

    @Test fun maximumStopClampsAndLatches() {
        val policy = DialHapticPolicy()
        policy.begin(MAX_TIMER_MINUTES - 1)
        assertEquals(HapticEvent.DIAL_BOUNDARY,
            policy.onMove(MAX_TIMER_MINUTES - 1, Int.MAX_VALUE, user, 0L))
        assertNull(policy.onMove(MAX_TIMER_MINUTES, Int.MAX_VALUE, user, 100L))
        assertEquals(HapticEvent.DIAL_MINUTE,
            policy.onMove(MAX_TIMER_MINUTES, MAX_TIMER_MINUTES - 1, user, 200L))
        assertEquals(HapticEvent.DIAL_BOUNDARY,
            policy.onMove(MAX_TIMER_MINUTES - 1, MAX_TIMER_MINUTES, user, 300L))
    }

    @Test fun fullHourTakesPriorityEvenWhenJumpAlsoReachesStop() {
        val policy = DialHapticPolicy(maxMinutes = 120)
        policy.begin(59)
        assertEquals(HapticEvent.DIAL_HOUR, policy.onMove(59, 120, user, 0L))
        assertNull(policy.onMove(120, 121, user, 100L))
    }

    @Test fun fastInputDoesNotQueueOrStackTicks() {
        val policy = DialHapticPolicy()
        policy.begin(1)
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(1, 2, user, 0L))
        assertNull(policy.onMove(2, 3, user, 10L))
        assertNull(policy.onMove(3, 4, user, 39L))
        assertEquals(HapticEvent.DIAL_FIVE_MINUTES, policy.onMove(4, 5, user, 40L))
        assertNull(policy.onMove(5, 5, user, 90L))
    }

    @Test fun rateSuppressedBoundaryDoesNotBuzzLaterWhileHeld() {
        val policy = DialHapticPolicy()
        policy.begin(2)
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(2, 1, user, 0L))
        assertNull(policy.onMove(1, 0, user, 10L))
        assertNull(policy.onMove(0, -1, user, 100L))
    }

    @Test fun idleAndCancelledGesturesHaveNoReleaseTick() {
        val policy = DialHapticPolicy()
        policy.begin(10)
        assertNull(policy.onMove(10, 10, user, 0L))
        assertNull(policy.end())
        policy.begin(10)
        policy.onMove(10, 11, user, 100L)
        assertNull(policy.end(cancelled = true))
        assertNull(policy.end())
    }

    @Test fun realAdjustmentSettlesOnce() {
        val policy = DialHapticPolicy()
        policy.begin(10)
        policy.onMove(10, 11, user, 0L)
        assertEquals(HapticEvent.DIAL_RELEASE, policy.end())
        assertNull(policy.end())
    }

    @Test fun clockRegressionCannotProduceAFeedbackBurst() {
        val policy = DialHapticPolicy()
        policy.begin(1)
        policy.onMove(1, 2, user, 100L)
        assertNull(policy.onMove(2, 3, user, 20L))
        assertEquals(HapticEvent.DIAL_MINUTE, policy.onMove(3, 4, user, 140L))
    }
}
