package dev.goutham.kitchentimer.ui

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import dev.goutham.kitchentimer.R
import dev.goutham.kitchentimer.haptics.DialChangeOrigin
import dev.goutham.kitchentimer.haptics.DialHapticPolicy
import dev.goutham.kitchentimer.model.*
import kotlin.math.*

internal val Barlow = FontFamily(Font(R.font.barlow_condensed, FontWeight.Medium))
internal fun formatTime(millis:Long):String {
    val seconds=ceil(millis.coerceAtLeast(0)/1000.0).toLong()
    val h=seconds/3600;val m=seconds%3600/60;val s=seconds%60
    return if(h>0)"$h:%02d:%02d".format(m,s) else "%02d:%02d".format(m,s)
}
internal fun durationWords(millis:Long):String {
    val mins=ceil(millis.coerceAtLeast(0)/60_000.0).toInt()
    val h=mins/60;val m=mins%60
    return listOfNotNull(if(h>0)"$h ${if(h==1)"hour" else "hours"}" else null,if(m>0||h==0)"$m ${if(m==1)"minute" else "minutes"}" else null).joinToString(" ")
}
private fun settingMillis(timer:TimerSnapshot)=if(timer.phase==TimerPhase.RINGING)timer.selectedDurationMillis else timer.remainingMillis
internal fun actionLabel(timer:TimerSnapshot)=when(timer.phase){
    TimerPhase.READY->"Start timer";TimerPhase.RUNNING->"Pause timer";TimerPhase.PAUSED->"Resume timer";TimerPhase.RINGING->"Stop alarm"
}

@Composable
fun TimerScreen(state:TimerAppState,capabilities:TimerCapabilities,onAction:(TimerAction)->Unit,onHaptic:(HapticEvent)->Unit) {
    val dark=when(state.settings.themeMode){ThemeMode.LIGHT->false;ThemeMode.DARK->true;ThemeMode.SYSTEM->isSystemInDarkTheme()}
    val focus=state.settings.focusMode
    var settingsOpen by rememberSaveable {mutableStateOf(false)}
    var sheetOpenHaptic by remember {mutableStateOf(false)}
    var pickerOpen by rememberSaveable {mutableStateOf(false)}
    var quickOpen by rememberSaveable {mutableStateOf(false)}
    // Focus removes every quick-set entry path, including a panel already open during an accessibility change.
    LaunchedEffect(focus){if(focus)quickOpen=false}
    val openSettings={onHaptic(HapticEvent.SHEET_THRESHOLD);sheetOpenHaptic=true;settingsOpen=true}
    val latestOpenSettings by rememberUpdatedState(openSettings)
    val openQuick={if(!focus){onHaptic(HapticEvent.QUICK_OPEN);quickOpen=true}}
    val background=if(focus)if(dark)listOf(Color(0xFF283637),Color(0xFF192526)) else listOf(Color(0xFFF6F3E4),Color(0xFFE9E7D6)) else listOf(Color(0xFF515852),Color(0xFF383E39),Color(0xFF292E2B),Color(0xFF232725))
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(background))
        .semantics { customActions=listOf(CustomAccessibilityAction("Open settings"){openSettings();true}) }
        .pointerInput(Unit) {
            // Child controls and the rotary dial consume their gestures first on Main pass.
            // Only an otherwise unhandled upward background swipe opens the sheet.
            awaitEachGesture {
                val down=awaitFirstDown(requireUnconsumed=false,pass=PointerEventPass.Main)
                if(down.isConsumed)return@awaitEachGesture
                while(true){
                    val event=awaitPointerEvent(PointerEventPass.Main)
                    if(event.changes.count{it.pressed}>1)break
                    val change=event.changes.firstOrNull{it.id==down.id}?:break
                    if(change.isConsumed||!change.pressed)break
                    val up=down.position.y-change.position.y
                    val sideways=abs(change.position.x-down.position.x)
                    if(up>48.dp.toPx()&&sideways<up*.55f){
                        change.consume();latestOpenSettings();break
                    }
                }
            }
        }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val availableWidth=maxWidth
            val availableHeight=maxHeight
            val regularDial=minOf(availableWidth-32.dp,availableHeight*.49f,440.dp)
            val regularLcdBudget=rememberLcdHeight(compact=false)
            val compactPortrait=regularDial+regularLcdBudget+261.dp>availableHeight
            val wide=availableWidth>availableHeight
            if(focus) {
                val d=minOf(availableWidth-12.dp,availableHeight-72.dp,600.dp)
                Box(Modifier.fillMaxSize().padding(bottom=16.dp),contentAlignment=Alignment.Center) {
                    InteractiveDial(Modifier.size(d),state,true,dark,onAction,onHaptic,openQuick)
                }
            } else if(wide) {
                val compactLcd=availableHeight<360.dp
                val lcdHeight=rememberLcdHeight(compactLcd)
                val controlViewport=(availableHeight-14.dp).coerceAtLeast(0.dp)
                // Fit the complete 88dp pushbutton row and 48dp rocker before adding gaps.
                // Two spare dp absorb rounding; smaller windows retain a real scroll fallback.
                val controlGap=((controlViewport-lcdHeight-136.dp-2.dp)/2).coerceIn(4.dp,16.dp)
                val controlsOverflow=lcdHeight+136.dp+controlGap*2>controlViewport
                Row(Modifier.fillMaxSize().padding(start=22.dp,end=22.dp,bottom=8.dp,top=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                    val d=minOf(availableHeight-72.dp,availableWidth*.52f,480.dp)
                    InteractiveDial(Modifier.size(d),state,false,dark,onAction,onHaptic,openQuick)
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState(),enabled=controlsOverflow),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(controlGap)) {
                        LcdReadout(state.timer,dark,Modifier.widthIn(max=260.dp).fillMaxWidth(),{pickerOpen=true},compact=compactLcd)
                        Controls(state.timer,onAction,onHaptic,compact=true)
                        AlarmRocker(state.settings.alarmMode,onAction,onHaptic)
                    }
                }
            } else if(compactPortrait) {
                // Reserve the real scaled text and control heights before sizing the face.
                // A scroll fallback handles tiny split windows without deforming physical controls.
                val lcdBudget=rememberLcdHeight(compact=true)
                val d=minOf(availableWidth-32.dp,(availableHeight-lcdBudget-228.dp).coerceAtLeast(160.dp))
                Column(Modifier.fillMaxSize().padding(horizontal=16.dp).padding(top=8.dp,bottom=16.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
                    LcdReadout(state.timer,dark,Modifier.widthIn(max=250.dp).width(availableWidth*.72f),{pickerOpen=true},compact=true)
                    Spacer(Modifier.height(8.dp))
                    InteractiveDial(Modifier.requiredSize(d),state,false,dark,onAction,onHaptic,openQuick)
                    Spacer(Modifier.height(16.dp))
                    Controls(state.timer,onAction,onHaptic,compact=true)
                    Spacer(Modifier.height(14.dp))
                    AlarmRocker(state.settings.alarmMode,onAction,onHaptic)
                    Spacer(Modifier.height(16.dp))
                    Grille()
                }
            } else {
                val d=regularDial
                Column(Modifier.fillMaxSize().padding(horizontal=16.dp).padding(top=18.dp,bottom=16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Spacer(Modifier.weight(.4f))
                    LcdReadout(state.timer,dark,Modifier.widthIn(max=270.dp).width(availableWidth*.62f),{pickerOpen=true})
                    Spacer(Modifier.weight(.7f))
                    Spacer(Modifier.height(18.dp))
                    InteractiveDial(Modifier.size(d),state,false,dark,onAction,onHaptic,openQuick)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.height(14.dp))
                    Controls(state.timer,onAction,onHaptic,compact=availableHeight<650.dp)
                    Spacer(Modifier.weight(.22f))
                    Spacer(Modifier.height(18.dp))
                    AlarmRocker(state.settings.alarmMode,onAction,onHaptic)
                    Spacer(Modifier.weight(.18f))
                    Spacer(Modifier.height(18.dp))
                    Grille()
                }
            }
            SettingsAccessTarget(Modifier.align(Alignment.BottomEnd),openSettings)
        }
    }
    if(settingsOpen) SettingsSheet(state.settings,capabilities,onAction,onHaptic,playOpenSettle=sheetOpenHaptic,onOpenSettled={sheetOpenHaptic=false},onDismiss={settingsOpen=false;onHaptic(HapticEvent.SHEET_SETTLE)})
    if(pickerOpen) DurationPicker(state.timer.remainingMillis,onHaptic,onDismiss={pickerOpen=false},onCommit={onAction(TimerAction.SetDuration(it));pickerOpen=false})
    if(quickOpen&&!focus) QuickSet(onDismiss={quickOpen=false},onSelect={onHaptic(HapticEvent.PRESET_SELECTED);onAction(TimerAction.SetDuration(it));quickOpen=false})
}

@Composable
private fun rememberLcdHeight(compact:Boolean):androidx.compose.ui.unit.Dp {
    val measurer=rememberTextMeasurer()
    val inherited=LocalTextStyle.current
    val density=LocalDensity.current
    return remember(compact,measurer,inherited,density) {
        val lineHeight=if(compact)40.sp else 52.sp
        val minuteStyle=inherited.copy(fontFamily=Barlow,fontSize=if(compact)36.sp else 50.sp,lineHeight=lineHeight)
        val hourStyle=minuteStyle.copy(fontSize=if(compact)30.sp else 40.sp)
        val digitHeight=maxOf(measurer.measure("88:88",style=minuteStyle,maxLines=1).size.height,measurer.measure("23:59:59",style=hourStyle,maxLines=1).size.height)
        val unitsStyle=inherited.copy(fontSize=if(compact)9.sp else 10.sp,lineHeight=if(compact)12.sp else inherited.lineHeight,letterSpacing=2.sp)
        val unitsHeight=measurer.measure("HR / MIN / SEC",style=unitsStyle,maxLines=1).size.height
        with(density){(digitHeight+unitsHeight).toDp()}.plus(if(compact)16.dp else 20.dp).coerceAtLeast(if(compact)66.dp else 86.dp)
    }
}

@Composable
private fun LcdReadout(timer:TimerSnapshot,dark:Boolean,modifier:Modifier,onClick:()->Unit,compact:Boolean=false) {
    val hours=timer.remainingMillis>=3_600_000
    val bg=if(dark)listOf(Color(0xFFD7E98B),Color(0xFFADC575),Color(0xFF8D9F5E)) else listOf(Color(0xFFA8AF92),Color(0xFFC1C6A8))
    Box(modifier.height(rememberLcdHeight(compact)).clip(RoundedCornerShape(5.dp)).background(Color(0xFF636C58)).padding(2.dp)
        .clickable(onClickLabel="Set duration",onClick=onClick)
        .semantics(mergeDescendants=true){contentDescription="Set duration";stateDescription=formatTime(timer.remainingMillis)}) {
        Box(Modifier.matchParentSize().background(Brush.linearGradient(bg)))
        if(dark)Box(Modifier.matchParentSize().background(Brush.radialGradient(listOf(Color(0xDDF1FFB4),Color(0x80D7ED86),Color.Transparent),center=Offset(0f,100f),radius=300f)))
        Canvas(Modifier.matchParentSize()){
            drawRect(Brush.verticalGradient(listOf(Color(0x70333E25),Color.Transparent),endY=12.dp.toPx()))
            drawRect(Brush.horizontalGradient(listOf(Color(0x50424B35),Color.Transparent),endX=5.dp.toPx()))
        }
        Column(Modifier.fillMaxSize().padding(vertical=if(compact)6.dp else 8.dp,horizontal=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            Text(formatTime(timer.remainingMillis),fontFamily=Barlow,fontSize=if(compact)if(hours)30.sp else 36.sp else if(hours)40.sp else 50.sp,lineHeight=if(compact)40.sp else 52.sp,color=if(dark)Color(0xFF28361E) else Color(0xFF354335),maxLines=1)
            Text(if(hours)"HR / MIN / SEC" else "MIN / SEC",fontSize=if(compact)9.sp else 10.sp,lineHeight=if(compact)12.sp else TextUnit.Unspecified,letterSpacing=2.sp,color=Color(0xFF354335),maxLines=1)
        }
        HardwareIconView(HardwareIcon.EDIT,Modifier.align(Alignment.TopEnd).padding(8.dp).size(12.dp),Color(0xFF68775D))
    }
}

@Composable
private fun InteractiveDial(modifier:Modifier,state:TimerAppState,focus:Boolean,dark:Boolean,onAction:(TimerAction)->Unit,onHaptic:(HapticEvent)->Unit,onQuick:()->Unit) {
    val latestState by rememberUpdatedState(state)
    val latestAction by rememberUpdatedState(onAction)
    val latestHaptic by rememberUpdatedState(onHaptic)
    val latestQuick by rememberUpdatedState(onQuick)
    val policy=remember{DialHapticPolicy()}
    fun adjust(target:Int) {
        val previous=(settingMillis(latestState.timer)/60_000.0).roundToInt()
        policy.begin(previous)
        latestAction(TimerAction.BeginSetting)
        policy.onMove(previous,target,DialChangeOrigin.USER_SETTING,SystemClock.uptimeMillis())?.let(latestHaptic)
        latestAction(TimerAction.SetDuration(target.coerceIn(0,MAX_TIMER_MINUTES)))
        policy.end()?.let(latestHaptic)
    }
    Box(modifier
        .semantics {contentDescription="Timer dial";stateDescription=durationWords(state.timer.remainingMillis);progressBarRangeInfo=ProgressBarRangeInfo((state.timer.remainingMillis/60_000f).coerceIn(0f,MAX_TIMER_MINUTES.toFloat()),0f..MAX_TIMER_MINUTES.toFloat(),MAX_TIMER_MINUTES-1);setProgress{adjust(it.roundToInt());true};customActions=listOf(CustomAccessibilityAction("Add one minute"){adjust((settingMillis(latestState.timer)/60_000.0).roundToInt()+1);true},CustomAccessibilityAction("Subtract one minute"){adjust((settingMillis(latestState.timer)/60_000.0).roundToInt()-1);true})}
        .onKeyEvent { event ->
            if(event.type!=KeyEventType.KeyDown)false else {
                val value=(settingMillis(latestState.timer)/60_000.0).roundToInt()
                val step=if(event.isShiftPressed)5 else 1
                when(event.key){Key.DirectionRight,Key.DirectionUp->{adjust(value+step);true};Key.DirectionLeft,Key.DirectionDown->{adjust(value-step);true};Key.MoveHome->{adjust(0);true};Key.MoveEnd->{adjust(MAX_TIMER_MINUTES);true};else->false}
            }
        }
        .focusable()
        .pointerInput(focus) {
            var lastTap=0L
            awaitEachGesture {
                val down=awaitFirstDown(requireUnconsumed=false)
                val center=Offset(size.width/2f,size.height/2f)
                val centered=(down.position-center).getDistance()<size.width*.105f
                var previousPosition=down.position
                var previousAngle=atan2(down.position.y-center.y,down.position.x-center.x)
                var total=settingMillis(latestState.timer)/60_000.0
                var previousMinute=total.roundToInt()
                var dragging=false
                var cancelled=false
                var ended=false
                try {
                    while(!ended){
                        val event=awaitPointerEvent()
                        if(event.changes.count{it.pressed}>1){cancelled=true;break}
                        val change=event.changes.firstOrNull{it.id==down.id}?:run{cancelled=true;break}
                        if(!change.pressed){ended=true;break}
                        val distance=(change.position-down.position).getDistance()
                        if(!dragging&&distance>viewConfiguration.touchSlop){
                            dragging=true;lastTap=0L;policy.begin(previousMinute);latestAction(TimerAction.BeginSetting)
                            if(centered)previousAngle=atan2(change.position.y-center.y,change.position.x-center.x)
                        }
                        if(dragging){
                            val radius=(change.position-center).getDistance()
                            if(radius>=size.width*.065f){
                                val angle=atan2(change.position.y-center.y,change.position.x-center.x)
                                var delta=(angle-previousAngle).toDouble()
                                if(delta>PI)delta-=2*PI
                                if(delta< -PI)delta+=2*PI
                                // At a hard stop, discard overshoot so reversing immediately releases it.
                                total=(total+delta*60/(2*PI)).coerceIn(-.6,MAX_TIMER_MINUTES+.6)
                                val requested=total.roundToInt()
                                policy.onMove(previousMinute,requested,DialChangeOrigin.USER_SETTING,change.uptimeMillis)?.let(latestHaptic)
                                val next=requested.coerceIn(0,MAX_TIMER_MINUTES)
                                if(next!=previousMinute){latestAction(TimerAction.SetDuration(next));previousMinute=next}
                                previousAngle=angle
                            }else previousAngle=atan2(change.position.y-center.y,change.position.x-center.x)
                            change.consume()
                        }else previousAngle=atan2(previousPosition.y-center.y,previousPosition.x-center.x)
                        previousPosition=change.position
                    }
                    if(!dragging&&!cancelled&&centered){
                        if(focus&&(latestState.timer.remainingMillis>0||latestState.timer.phase==TimerPhase.RINGING)){latestHaptic(HapticEvent.BUTTON_PRESS);latestAction(if(latestState.timer.phase==TimerPhase.RINGING)TimerAction.StopAlarm else TimerAction.ToggleRun);latestHaptic(HapticEvent.BUTTON_RELEASE)}
                        else if(!focus) {
                            val now=SystemClock.uptimeMillis()
                            if(now-lastTap in 1..viewConfiguration.doubleTapTimeoutMillis){latestQuick();lastTap=0L}else lastTap=now
                        }
                    }
                }finally{if(dragging)policy.end(cancelled||!ended)?.let(latestHaptic)}
            }
        }
    ) {
        DialDrawing(Modifier.fillMaxSize(),state.timer.remainingMillis,focus,dark)
        Box(Modifier.align(Alignment.Center).size(56.dp).semantics {
            role=Role.Button
            contentDescription=if(focus)actionLabel(state.timer) else "Quick set"
            val available=!focus||state.timer.remainingMillis>0||state.timer.phase==TimerPhase.RINGING
            if(!available)disabled()
            onClick(label=if(focus)actionLabel(state.timer) else "Open quick set"){
                if(!available)false else {if(focus){onHaptic(HapticEvent.BUTTON_PRESS);onAction(if(state.timer.phase==TimerPhase.RINGING)TimerAction.StopAlarm else TimerAction.ToggleRun);onHaptic(HapticEvent.BUTTON_RELEASE)}else onQuick();true}
            }
        }.onKeyEvent { event ->
            if(event.type==KeyEventType.KeyUp&&(event.key==Key.Enter||event.key==Key.Spacebar)){
                if(focus){if(state.timer.remainingMillis>0||state.timer.phase==TimerPhase.RINGING){onHaptic(HapticEvent.BUTTON_PRESS);onAction(if(state.timer.phase==TimerPhase.RINGING)TimerAction.StopAlarm else TimerAction.ToggleRun);onHaptic(HapticEvent.BUTTON_RELEASE)}}else onQuick();true
            }else false
        }.focusable())
    }
}

@Composable
private fun Controls(timer:TimerSnapshot,onAction:(TimerAction)->Unit,onHaptic:(HapticEvent)->Unit,compact:Boolean) {
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)){
        PhysicalButton(Modifier.requiredSize(if(compact)72.dp else 80.dp),false,HardwareIcon.RESET,"Reset timer",true,onHaptic){onAction(TimerAction.Reset)}
        PhysicalButton(Modifier.requiredSize(if(compact)88.dp else 98.dp),true,when(timer.phase){TimerPhase.RUNNING->HardwareIcon.PAUSE;TimerPhase.RINGING->HardwareIcon.STOP;else->HardwareIcon.PLAY},actionLabel(timer),timer.remainingMillis>0||timer.phase==TimerPhase.RINGING,onHaptic){
            if(timer.phase==TimerPhase.RINGING){onAction(TimerAction.StopAlarm);onHaptic(HapticEvent.ALARM_STOP)}else onAction(TimerAction.ToggleRun)
        }
    }
}

@Composable
private fun PhysicalButton(modifier:Modifier,primary:Boolean,icon:HardwareIcon,label:String,enabled:Boolean,onHaptic:(HapticEvent)->Unit,onClick:()->Unit) {
    var held by remember{mutableStateOf(false)}
    val travel by animateFloatAsState(if(held)1f else 0f,tween(90),label="Cap travel")
    val light by animateFloatAsState(if(held)1f else 0f,tween(if(held)45 else 320),label="Button lamp")
    val latestClick by rememberUpdatedState(onClick)
    val latestHaptic by rememberUpdatedState(onHaptic)
    Box(modifier.semantics{role=Role.Button;contentDescription=label;if(!enabled)disabled();onClick{if(enabled){onHaptic(if(primary)HapticEvent.BUTTON_PRESS else HapticEvent.RESET);onClick();onHaptic(HapticEvent.BUTTON_RELEASE);true}else false}}
        .pointerInput(enabled){if(enabled)detectTapGestures(onPress={held=true;latestHaptic(if(primary)HapticEvent.BUTTON_PRESS else HapticEvent.RESET);val released=tryAwaitRelease();held=false;if(released){latestClick();latestHaptic(HapticEvent.BUTTON_RELEASE)}})}
        .onKeyEvent{if(enabled&&it.type==KeyEventType.KeyUp&&(it.key==Key.Enter||it.key==Key.Spacebar)){onHaptic(if(primary)HapticEvent.BUTTON_PRESS else HapticEvent.RESET);onClick();onHaptic(HapticEvent.BUTTON_RELEASE);true}else false}.focusable(enabled)){
        PushButtonDrawing(Modifier.fillMaxSize(),primary,travel,light,icon,enabled)
    }
}

@Composable
private fun AlarmRocker(mode:AlarmMode,onAction:(TimerAction)->Unit,onHaptic:(HapticEvent)->Unit) {
    val sound=mode==AlarmMode.SOUND
    val interactions=remember { MutableInteractionSource() }
    Row(Modifier.heightIn(min=48.dp).semantics(mergeDescendants=true){contentDescription="Alarm mode";stateDescription=if(sound)"Sound" else "Vibrate";role=Role.Switch;toggleableState=if(sound)androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off}
        .clickable(interactionSource=interactions,indication=null) {onHaptic(HapticEvent.ROCKER_SWITCH);onAction(TimerAction.SetAlarmMode(if(sound)AlarmMode.VIBRATE else AlarmMode.SOUND))},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)){
        HardwareIconView(HardwareIcon.SOUND,Modifier.size(24.dp),if(sound)Color(0xFFE5EADB) else Color(0xFF929B8C))
        Box(Modifier.size(68.dp,34.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Color(0xFF070D08),Color(0xFF172017)))).padding(4.dp)){
            Box(Modifier.align(if(sound)Alignment.CenterStart else Alignment.CenterEnd).size(29.dp,26.dp).clip(RoundedCornerShape(5.dp)).background(Brush.linearGradient(listOf(Color(0xFFC0C9B5),Color(0xFF74836C))))) {
                Canvas(Modifier.fillMaxSize()){for(x in listOf(.37f,.63f)){drawLine(Color(0xFF4C5C43),Offset(size.width*x,size.height*.28f),Offset(size.width*x,size.height*.72f),1.dp.toPx());drawLine(Color(0xFFCAD6BD),Offset(size.width*x+1.dp.toPx(),size.height*.28f),Offset(size.width*x+1.dp.toPx(),size.height*.72f),.5.dp.toPx())}}
            }
        }
        HardwareIconView(HardwareIcon.VIBRATE,Modifier.size(24.dp),if(!sound)Color(0xFFE5EADB) else Color(0xFF929B8C))
    }
}

@Composable
private fun Grille(){Canvas(Modifier.size(64.dp,13.dp)){repeat(9){i->val x=i*size.width/9;drawRoundRect(Color(0xFF121A0E),Offset(x,0f),androidx.compose.ui.geometry.Size(3.dp.toPx(),size.height),androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()));drawLine(Color(0x554E6344),Offset(x,size.height),Offset(x+2.dp.toPx(),size.height),1.dp.toPx())}}}

@Composable
private fun SettingsAccessTarget(modifier:Modifier,onOpen:()->Unit){
    val latestOpen by rememberUpdatedState(onOpen)
    Box(modifier.size(48.dp).semantics{role=Role.Button;contentDescription="Open settings";onClick{onOpen();true}}.pointerInput(Unit){
        awaitEachGesture{val down=awaitFirstDown();var opened=false;while(true){val event=awaitPointerEvent();val change=event.changes.firstOrNull{it.id==down.id}?:break;if(!change.pressed){if(!opened)latestOpen();break};if(!opened&&down.position.y-change.position.y>20.dp.toPx()){opened=true;latestOpen();change.consume()}}}
    }.onKeyEvent{if(it.type==KeyEventType.KeyUp&&(it.key==Key.Enter||it.key==Key.Spacebar)){onOpen();true}else false}.focusable())
}
