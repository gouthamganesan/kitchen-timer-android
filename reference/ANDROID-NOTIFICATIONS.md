# Android notification design constraints

Checked against Android Developers on 17 November 2026. This is an HTML design preview, not a posted native notification.

## Chosen visual implementation

A decorated custom ongoing notification keeps Android's header and action row. The app supplies a miniature red-disc image and readable countdown. Compact custom content is 48dp; expanded custom content is 112dp, beneath the documented 252dp constrained expanded allowance. The mock uses CSS pixels as a 1:1 design proxy for dp; actual text scaling and OEM layouts need device verification. RemoteViews with ImageView and Chronometer/TextView can implement this structure. Use the system text appearance outside the fixed-contrast miniature LCD. See [custom layout guidance](https://developer.android.com/develop/ui/views/notifications/custom-notification).

The dial would be rendered to a bitmap for ImageView, refreshed when the visible minute changes. Use a native countdown Chronometer for ticking digits instead of reposting a notification each second. Link action PendingIntents to the same timer state; these are design recommendations. [Chronometer API](https://developer.android.com/reference/android/app/Notification.Builder#setUsesChronometer(boolean)).

## Platform boundaries

Android 13+ needs notification permission for this non-exempt surface. Use a quiet countdown channel and a separate completion-alarm channel; user channel choices take precedence. [Permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission), [channels](https://developer.android.com/develop/ui/compose/notifications/create-notification).

Ongoing does not guarantee permanent visibility: Android 14 permits individual dismissal while unlocked. Clearing all and locked-screen behavior differ. Respect a user's dismissal. [Dismissal behavior](https://developer.android.com/about/versions/14/behavior-changes-all#non-dismissable-notifications).

Promoted Live Updates reject custom content views. This dial-rich design therefore does not promise a promoted status-bar chip. A separate standard-style variant could be explored for eligible devices and use cases. [Live Update requirements](https://developer.android.com/develop/ui/views/notifications/live-update).

The notification is a display of timer state, not the scheduling mechanism. The native build still needs an appropriate alarm/scheduling implementation; a foreground-service type has not been chosen here.
