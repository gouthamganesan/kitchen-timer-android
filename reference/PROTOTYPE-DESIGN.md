# Red disc timer

The user selected the red-disc direction. The original three-way comparison remains in studies-v1.html; index.html is the current iteration.

## Material and form

Realistic hardware skeuomorphism: charcoal rubber housing (#383e39 / #292e2b), recessed ivory dial (#ece9d9), red time disc (#c73b2d), and a green LCD readout. Upper-left lighting, editable CSS/SVG geometry, embedded Barlow Condensed font. No flattened timer images or external runtime dependencies.

The main screen removes the Kitchen Timer title, Ready to Wind status and full-hour counter as requested. Time remains explicit in the readout. Reset and Start are round physical plungers with recessed sockets, raised caps, sidewalls and 7px press travel. Icons replace printed labels, with play/pause/stop states and momentary warm backlighting on press; Reset uses a return-arrow icon with yellow-green illumination. Speaker/vibration icons replace text around the switch.

## Duration picker

A custom component follows Material's clock-picker interaction and the timer's material palette. Hours and minutes are selectable fields. Tap or drag the circular clock; picking hours advances to minutes. Hours use an outer 0–11 ring and inner 12–23 ring. Minutes support exact one-minute selection. The keyboard button switches to direct numeric entry, retaining draft values. Set commits; Cancel or Escape discards. This is a duration, so there is no AM/PM selection.

Native dialog supplies focus containment and dismissal. The circular control supports arrow keys, Shift for five-unit steps and Home/End. Exact non-five-minute values replace nearby dial labels to avoid overlap.

## Hour layers

Completed hours fill the disc; the partial hour uses the next darker shade. For 2 h 25, a full disc has the second-layer shade and a 150° sector has the third. Exact-hour boundaries have no partial sector. Higher-hour shade differences taper; the LCD remains authoritative for exact duration. The separate full-hour counter is intentionally removed.

## Scope and validation

The 0–23 h 59 min range remains a prototype assumption. Start/Pause/Resume, Reset, repeated winding, layered colours and foreground repeating alarms work. Browser sound/vibration require supported capabilities and user activation; Android background scheduling is not implemented.

Verified in Chrome: requested removals and icons; outer/inner hour rings; minute precision; keyboard mode, validation and retained values; cancellation and focus restoration; visible button press travel; countdown and pause; reset; two full turns; exact-hour boundary; alarm preview/stop. No horizontal overflow or clipped device content at 320, 390, 900 and 1200px. The picker fits a 320 × 568 viewport. Captures live in review/v2/.

## Notification companion

The prototype includes collapsed and expanded Android-style notification previews, a light/dark shade toggle, shared timer state, and working Pause/Resume/Reset actions. At rest the cards are explicitly marked as a design preview. Native limits and source references are in ANDROID-NOTIFICATIONS.md.

## Pull-up settings and focus

Tap or pull upward from the bottom grip to open the settings sheet. Stay awake uses the browser Screen Wake Lock API and reports unsupported or denied requests. It releases when the document is hidden and attempts reacquisition on return. Android can use Compose keepScreenOn or FLAG_KEEP_SCREEN_ON while the activity is visible: https://developer.android.com/develop/background-work/background-tasks/awake/screen-on.

Focus mode hides the readout, status bar, buttons, alarm-mode switch and grille. Only the large dial and settings grip remain on the dial's pale ivory surface. Tap the centre to start/pause; wind to adjust. Theme is a general setting with Light, Dark and Follow system choices. Follow system is the default and responds immediately to system appearance changes; explicit Light/Dark choices remain pinned. Focus mode retains the original night design: a deep charcoal-teal surface with an amber disc. Quick set is unavailable in Focus mode; the centre remains a single-tap start/pause control. The original boolean preference migrates to Follow system. Preferences are stored locally; the timer duration is unchanged by these modes.

## Hardware illumination and quick set

In the regular hardware view, Dark switches on only the LCD and dial backlights. The casing, controls and red time disc keep their original materials. The LCD uses yellow-green illumination concentrated at its left edge, with a brighter emitter and a gradual falloff; the analog face uses warm white LED pools beneath its bezel. The knob catches a warm ivory rim light. These gradients and local light spills are intentional physical-light simulations required by the user's brief.

Double-tap the centre knob in the regular view to reveal fixed presets: 1, 3, 5, 10, 15, 20, 30 and 60 minutes. Choosing one sets the timer ready; it does not start automatically. Opening or cancelling the panel leaves an existing countdown running. The knob also opens the panel with Enter/Space for keyboard access. Focus mode has no preset panel.

Verified this iteration: 25→40 minutes with a quarter-turn; icon state and momentary lamp activation; preset selection without auto-start; opening presets during a countdown without pausing it; theme visible with Focus off; original light and dark Focus faces restored; no quick-set control or panel in Focus mode.
