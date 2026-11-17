# UI progress

2026-11-17: Read AGENTS, requirements, architecture, handoff and frozen prototype. Approved reference is the visual authority. Starting Canvas hardware rendering and Compose interactions. No build or visual check has run yet.

Next: implementation, integrated compile, screenshots via integration agent.

2026-11-17 implementation: HardwareDrawing.kt, TimerScreen.kt and TimerDialogs.kt now written. Approved materials, local backlight origins, responsive composition, setting-only rotary dispatch, physical icon buttons, Material settings sheet, fixed quick set and circular/keyboard duration picker implemented. Root is running integrated compile/device checks. Added keyboard focusability and zero-duration disabled semantics; centered winding initializes angle after movement so leaving the centre does not jump the time. First dialog write hit automatic review timeout; smaller staged copy succeeded. No device driven by UI agent.

2026-11-17 first rendered inspection: Opened artifacts/initial-launch/2026-11-17_165100/00-launch/takeScreenshot/initial-native.png (regular dark/system on emulator). UI compiles; native screen shows all main controls. Found lower 30 mark crossing the face edge and sharp knob/button highlight arcs. Reduced regular number radius146→140 and tick radii slightly, softened highlights and knob contact shadow, added inset paper/bezel contact shading. Root owns second screenshot and interaction validation. No layout/behavior scope change.

2026-11-17 QA correction: Settings sheet's expansion settle now requires an unsaved explicit user-open token, consumed once at Expanded. A rememberSaveable sheet restored by rotation or system-theme recreation opens silently. This closes passive haptic dispatch on restoration. UI frozen for integrated final candidate build.

2026-11-17 gesture-state correction: When winding a ringing timer, pointer and keyboard/accessibility adjustment baselines now use selectedDurationMillis, matching BeginSetting's alarm acknowledgment semantics. This avoids a restore-to-old-duration then jump-to-one-minute mismatch on first crossing. Ready for root's final candidate build.

2026-11-17 picker correction: Clock pointer selections now always reach the draft setter, which performs the correct raw-draft equality check. An invalid typed value (24 hours/99 minutes) can now be corrected by tapping the clamped value visibly selected on returning to the clock. Previously the drawing's clamped equality could suppress that correction. Root/QA regression validation pending.

2026-11-17 lint correction: Duration picker maximum height now derives from LocalWindowInfo container size through LocalDensity, rather than LocalConfiguration screenHeightDp, for accurate split-window sizing. Await root final validation; no device state changed by this agent.

2026-11-17 compact-device correction: Reviewed compact-layout/2026-11-17_171940 captures at320×568dp/font1.3. Confirmed compressed button caps, hidden rocker and offscreen picker actions. Added a height-budgeted compact portrait composition with smaller LCD typography and measured dial budget, fixed circular button sizes and bounded scroll fallback. Landscape control column scrolls rather than compressing. Picker actions now sit outside its scroll body and remain pinned. Normal layout/materials unchanged. Root owns candidate compile and emulator recheck; user phone untouched.

2026-11-17 compact candidate compile: :app:compileDebugKotlin PASS (artifacts/ui-compact-compile.log), two non-null assertion warnings only. Inspected both landscape-before screenshots; control squeeze matches compact portrait cause, now addressed by fixed sizes and scrolling right column. Settings sheet was scrollable and retained Back/Close; no unrelated setting design changes.

2026-11-17 user refinement: Removed the visible app settings dash in both regular/focus modes (OS navigation bar untouched). Upward swipes on screen background now open settings via ancestor Main-pass detection; consumed dial/control gestures retain priority. An invisible48dp bottom target and root accessibility custom action preserve Open settings for keyboard/TalkBack/Maestro. Combined with compact fixes; compile/runtime verification next.

2026-11-17 combined compact+swipe candidate: :app:compileDebugKotlin PASS (artifacts/ui-swipe-compile.log). Sources frozen for root runtime checks. No emulator or phone interaction from UI agent.


2026-11-17 user spacing refinement: Inspected normal and compact-fixed screenshots. Reclaimed the removed grip's48dp reservation with16dp bottom padding. Normal layout distributes flexible space between buttons/rocker/grille instead of trailing empty space; grille sits lower. Compact keeps its proven dial/LCD/button dimensions and exactly exchanges32dp bottom reservation for8/12/12dp additional gaps after dial/buttons/rocker. Landscape reclaims32dp room. Invisible Open settings target moved to48dp bottom-right corner so it cannot intercept lowered controls. :app:compileDebugKotlin PASS (artifacts/ui-spacing-compile.log). First write approval timed out; simple staged copy retry succeeded. Root owns runtime recheck/device update; this agent touched no device.


2026-11-17 winding stability: Root gesture check passed background sheet swipe and multi-turn winding without hijack, but hour formatting exposed variable LCD intrinsic height. Replaced minimum-only LCD height with a duration-independent measured maximum of both digit font styles plus unit row/padding. Normal/compact layout budgets share the same measurement. This preserves font scaling/materials while preventing dial movement at60-minute boundaries. :app:compileDebugKotlin PASS (artifacts/ui-stable-lcd-compile.log). Root final runtime check/build pending.


2026-11-17 final landscape correction: Reviewed artifacts/final-compact/2026-11-17_195317/04-compact/takeScreenshot/compact-landscape.png. The two fixed16dp gaps plus measured LCD/88dp buttons/48dp rocker exceeded the landscape viewport and clipped the rocker at rest. Landscape now computes gaps from its actual remaining height, uses6dp top/8dp bottom edge padding, and retains scrolling only when the full controls still cannot fit. Portrait layout unchanged. Compile and root runtime recheck pending. Phone/wireless pairing remain untouched/deferred.


2026-11-17 landscape fit compile: :app:compileDebugKotlin PASS (artifacts/ui-landscape-fit-compile.log). Candidate handed to root for screenshot and rocker/settings interaction verification. No device operations performed.


2026-11-17 rocker press refinement: Identified the unpadded dark rectangle as AlarmRocker row's inherited Material clickable indication. Set an explicit remembered interaction source with indication=null. The physical slider, active icons, snap haptic, semantics, touch area and layout are unchanged. Root owns build/runtime pressed-state verification and delivery. No device, git, build configuration or public-preparation files changed. Previous final landscape check was confirmed passing by integration, including rocker toggles both ways and settings.
