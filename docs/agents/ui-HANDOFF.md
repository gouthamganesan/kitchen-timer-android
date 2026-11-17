# UI handoff

2026-11-17, implementation and first rendered correction pass complete. Integrated final-candidate validation remains root-owned.

## Files and contract
- app/src/main/java/dev/goutham/kitchentimer/ui/TimerScreen.kt: public TimerScreen(state, capabilities, onAction, onHaptic), responsive regular/focus layout, LCD, rotary pointer/keyboard/accessibility paths, physical controls and rocker/grip.
- HardwareDrawing.kt: native Canvas/gradient dial, per-hour red/amber layers, scale and knob, authored icons and plunger materials.
- TimerDialogs.kt: general settings ModalBottomSheet, fixed quick-set Dialog, circular and keyboard duration picker with draft-only changes and Cancel/Set semantics.

Uses shared TimerContracts and DialHapticPolicy.begin/onMove/end. Dial policy receives USER_SETTING only, never countdown/render changes. MainActivity owns permissions, state and visible-only keep-screen-on.

## Geometry and materials
- Regular housing linear gradient #515852 -> #383E39 -> #292E2B -> #232725.
- Regular dial normalized 360-unit Canvas, 44-unit bumper radius, ivory face inset24, red disc radius101, printed radius140, ticks114/127 and knob26. Focus drops bumper/face and uses disc111, printed155.
- Dial width is minimum of available width minus32dp, 49% of safe height and440dp. Landscape uses side-by-side dial and controls. Focus dial is centered and grows to min(width-12dp,height-72dp,600dp).
- LCD 62% viewport width capped270dp, Barlow Condensed50sp or40sp with hours. Primary cap98dp, reset80dp, compact88/72dp. Touch targets at least48dp (knob56dp).
- Dark regular keeps casing and red layers; LCD left edge emitter yellow-green, analog side diffuser pools white-yellow. Focus dark remains charcoal-teal/amber.

## Validation and remaining work
Initial UI compile succeeded as part of root build and initial native screenshot was inspected at artifacts/initial-launch/2026-11-17_165100/00-launch/takeScreenshot/initial-native.png. First visual pass corrected bottom label/bezel contact, softened sharp specular arcs/contact shadows and added inner-face shading. Root owns final integrated compile/lint, second screenshot and Maestro checks. Physical haptic feel is untested. Final native visual review can use approved prototype, requirements, this geometry and root screenshots; no HTML/CSS detector applies to Kotlin.

## Important semantics for QA
Set duration; Timer dial; Quick set (regular only); Start timer/Pause timer/Resume timer/Stop alarm; Reset timer; Alarm mode; Open settings; Close settings; Stay awake; Focus mode; Theme options Light/Dark/Follow system; Type duration/Use clock dial; Hours/Minutes input; Select hours/Select minutes; Hours dial/Minutes dial; Cancel/Set; Set X minutes presets. Native system permission flows are owned by Activity.

## Correctness details for pickup
Settings rows are whole-row toggleable with a single merged semantic target. Sheet expansion feedback requires a non-saveable explicit user-open token, so rotation/theme restoration is silent. Focus zero start is disabled on touch/accessibility/keyboard paths. Winding from RINGING uses selected duration, matching BeginSetting. Invalid numeric picker drafts can be corrected by selecting the displayed clock value; equality suppression belongs to raw draft setter, not the clamped rendering.

## Compact revision
320×568dp/font1.3 runtime evidence exposed control compression and offscreen picker actions. TimerScreen now compares available height against scaled LCD/control needs, switches only constrained portrait windows to compact LCD typography and a budgeted dial, keeps plunger dimensions square with requiredSize, and supplies scrolling fallback for extremely short windows. Landscape controls scroll within their available column. DurationPicker body scrolls independently of the pinned Type/Cancel/Set footer. :app:compileDebugKotlin PASS recorded in artifacts/ui-compact-compile.log. Root's compact screenshot/interaction rerun remains the visual acceptance evidence; no phone/device was touched by UI agent.

## Latest user refinement: sheet gesture
The visible gray app settings dash is removed in both modes; native OS navigation remains untouched. TimerScreen's ancestor Main-pass pointer listener opens settings on an unconsumed upward background swipe after48dp, with strong vertical intent. It yields immediately to consumed rotary/control gestures and multitouch. SettingsAccessTarget is an invisible48dp bottom button retaining Open settings semantics and keyboard activation; root also exposes a custom accessibility action. Compile PASS in artifacts/ui-swipe-compile.log. Root owns runtime swipe verification and final combined APK. The earlier visible-grip reference is superseded by this user correction.


## Spacing after dash removal
Latest user request balances the freed lower area. Bottom content padding is16dp across layouts. Normal portrait distributes remaining height after primary controls and rocker, with the grille anchoring the bottom. Compact swaps32dp of the old grip reservation for8/12/12dp extra gaps, preserving the passing compact total budget and hero sizes. Landscape has32dp more control viewport height. Open settings remains accessible through a48dp invisible bottom-right target and root custom action; it no longer covers the full-width bottom region. Compile PASS: artifacts/ui-spacing-compile.log. Root screenshot/device check remains required for this refinement.


## Stable LCD geometry
rememberLcdHeight measures both minute and hour styles plus units using Compose text metrics, chooses the maximum, adds existing padding and fixes the LCD to that height. Timer layout budgets use this same value. Current duration is not an input, so crossing hour boundaries cannot move the dial or change compact layout. Latest spacing preserved. Compile evidence: artifacts/ui-stable-lcd-compile.log.


## Final landscape fit correction
The previous scrolling landscape column left the rocker partially clipped at rest on320×568dp/font1.3. Landscape alone now reserves the measured LCD,88dp pushbutton row and48dp rocker before choosing4–16dp gaps, with2dp rounding headroom and6dp top/8dp bottom padding. Scrolling remains available only when even minimum gaps cannot fit, leaving background swipe-to-settings free when controls fit. Portrait balance, sizes and materials unchanged. Compile PASS: artifacts/ui-landscape-fit-compile.log. Root owns final screenshot/action rerun; phone install and wireless pairing remain deferred.


## Rocker press indication
AlarmRocker now opts out of the default rectangular Material ripple with a remembered MutableInteractionSource and indication=null. Its physical switch position, selected icon and snap haptic carry feedback; minimum48dp row target and accessible Switch semantics remain intact. No spacing or portrait/landscape geometry changed. Integration owns build and held-press/release screenshot verification for this small refinement.
