package dev.goutham.kitchentimer.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import dev.goutham.kitchentimer.model.HapticEvent

/** Touch feedback only. Repeating alarm vibration is separately owned by the alarm service. */
class TimerHaptics(private val view: View) {
    fun perform(event: HapticEvent) {
        // Suppress detached/hidden owners. Do not require window focus: Compose dialogs
        // temporarily own focus while dispatching feedback through the Activity root view.
        if (!view.isAttachedToWindow || !view.isShown ||
            !view.isHapticFeedbackEnabled || view.windowVisibility != View.VISIBLE
        ) return
        // No IGNORE_* flags and no raw-vibration fallback: false can mean the user disabled it,
        // or the device intentionally suppresses a tiny tick it cannot reproduce comfortably.
        view.performHapticFeedback(feedbackConstant(event))
    }

    private fun feedbackConstant(event: HapticEvent): Int = when (event) {
        HapticEvent.DIAL_MINUTE, HapticEvent.PICKER_TICK ->
            if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
            else HapticFeedbackConstants.CLOCK_TICK
        HapticEvent.DIAL_FIVE_MINUTES, HapticEvent.SETTINGS_SELECT ->
            if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_TICK
            else HapticFeedbackConstants.CONTEXT_CLICK
        HapticEvent.DIAL_HOUR, HapticEvent.PRESET_SELECTED, HapticEvent.PICKER_CONFIRM,
        HapticEvent.QUICK_OPEN, HapticEvent.ALARM_STOP ->
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
        HapticEvent.DIAL_BOUNDARY ->
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS
        HapticEvent.RESET -> HapticFeedbackConstants.LONG_PRESS
        HapticEvent.BUTTON_PRESS -> HapticFeedbackConstants.VIRTUAL_KEY
        HapticEvent.BUTTON_RELEASE ->
            if (Build.VERSION.SDK_INT >= 27) HapticFeedbackConstants.VIRTUAL_KEY_RELEASE
            else HapticFeedbackConstants.CLOCK_TICK
        HapticEvent.ROCKER_SWITCH ->
            if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.CONTEXT_CLICK
        HapticEvent.SHEET_THRESHOLD ->
            if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
            else HapticFeedbackConstants.CONTEXT_CLICK
        HapticEvent.SHEET_SETTLE, HapticEvent.DIAL_RELEASE ->
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.GESTURE_END else HapticFeedbackConstants.CLOCK_TICK
    }
}
