package dev.goutham.kitchentimer.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.goutham.kitchentimer.R
import dev.goutham.kitchentimer.model.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSheet(settings:TimerSettings,capabilities:TimerCapabilities,onAction:(TimerAction)->Unit,onHaptic:(HapticEvent)->Unit,playOpenSettle:Boolean,onOpenSettled:()->Unit,onDismiss:()->Unit){
    val sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)
    val latestHaptic by rememberUpdatedState(onHaptic)
    val shouldSettle by rememberUpdatedState(playOpenSettle)
    val latestSettled by rememberUpdatedState(onOpenSettled)
    // An unsaved intent token distinguishes a user opening from restored sheet state.
    LaunchedEffect(sheetState){snapshotFlow{sheetState.currentValue}.collect{if(it==SheetValue.Expanded&&shouldSettle){latestHaptic(HapticEvent.SHEET_SETTLE);latestSettled()}}}
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=sheetState,containerColor=Ivory,contentColor=Ink,dragHandle={BottomSheetDefaults.DragHandle(color=Color(0xFFB4BBA7))}){
        Column(Modifier.fillMaxWidth().widthIn(max=560.dp).align(Alignment.CenterHorizontally).verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=28.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Text("Settings",Modifier.weight(1f),fontFamily=Barlow,fontSize=30.sp,color=Ink)
                IconButton(onClick=onDismiss,modifier=Modifier.semantics{contentDescription="Close settings"}){HardwareIconView(HardwareIcon.CLOSE,Modifier.size(22.dp))}
            }
            SettingSwitch("Stay awake","Keep the screen on while the timer is open.",settings.stayAwake){if(it!=settings.stayAwake){onHaptic(HapticEvent.SETTINGS_SELECT);onAction(TimerAction.SetStayAwake(it))}}
            HorizontalDivider(color=Color(0xFFD2D7C7))
            SettingSwitch("Focus mode","Just the dial. Tap its centre to start or pause.",settings.focusMode){if(it!=settings.focusMode){onHaptic(HapticEvent.SETTINGS_SELECT);onAction(TimerAction.SetFocusMode(it))}}
            HorizontalDivider(color=Color(0xFFD2D7C7))
            Text("Theme",Modifier.padding(top=22.dp,bottom=12.dp),fontSize=16.sp,color=Ink)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFD8DDCC)).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)){
                listOf(ThemeMode.LIGHT to "Light",ThemeMode.DARK to "Dark",ThemeMode.SYSTEM to "Follow system").forEach{(theme,label)->
                    val selected=settings.themeMode==theme
                    Box(Modifier.weight(if(theme==ThemeMode.SYSTEM)1.45f else 1f).heightIn(min=52.dp).clip(RoundedCornerShape(8.dp)).background(if(selected)Color(0xFFF8F5E7) else Color.Transparent).selectable(selected,role=Role.RadioButton){if(!selected){onHaptic(HapticEvent.SETTINGS_SELECT);onAction(TimerAction.SetTheme(theme))}}.padding(horizontal=6.dp,vertical=10.dp),contentAlignment=Alignment.Center){Text(label,color=if(selected)Ink else Color(0xFF526048),fontSize=13.sp,textAlign=TextAlign.Center)}
                }
            }
            Text("Dark lights the displays. Focus mode uses its own night face.",Modifier.padding(top=10.dp),fontSize=12.sp,lineHeight=18.sp,color=Color(0xFF58664D))
            if(!capabilities.notificationsGranted||!capabilities.exactAlarmsGranted){
                HorizontalDivider(Modifier.padding(top=22.dp,bottom=12.dp),color=Color(0xFFD2D7C7))
                if(!capabilities.notificationsGranted)PermissionRow("Timer notifications","See the countdown and stop the alarm outside the app.","Allow notifications"){onAction(TimerAction.RequestNotifications)}
                if(!capabilities.exactAlarmsGranted)PermissionRow("Precise alarms","Allow the timer to ring at the selected time while your phone is locked.","Allow precise alarms"){onAction(TimerAction.RequestExactAlarms)}
            }
        }
    }
}

@Composable
private fun SettingSwitch(title:String,description:String,checked:Boolean,onChange:(Boolean)->Unit){
    Row(Modifier.fillMaxWidth().toggleable(checked,role=Role.Switch,onValueChange=onChange).semantics(mergeDescendants=true){contentDescription=title}.padding(vertical=18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
        Column(Modifier.weight(1f)){
            Text(title,fontSize=16.sp,color=Ink)
            Text(description,Modifier.padding(top=5.dp),fontSize=12.sp,lineHeight=18.sp,color=Color(0xFF58664D))
        }
        Switch(checked=checked,onCheckedChange=null,modifier=Modifier.clearAndSetSemantics{},colors=SwitchDefaults.colors(checkedTrackColor=Sage,checkedThumbColor=Color(0xFFF7F5E5),uncheckedTrackColor=Color(0xFFBEC7B2),uncheckedThumbColor=Color(0xFFF7F5E5),uncheckedBorderColor=Color(0xFF8B987B)))
    }
}

@Composable
private fun PermissionRow(title:String,description:String,button:String,onClick:()->Unit){
    Column(Modifier.padding(vertical=8.dp)){
        Text(title,fontSize=15.sp,color=Ink)
        Text(description,Modifier.padding(top=4.dp),fontSize=12.sp,lineHeight=18.sp,color=Color(0xFF58664D))
        TextButton(onClick=onClick,colors=ButtonDefaults.textButtonColors(contentColor=Red)){Text(button)}
    }
}

@Composable
internal fun QuickSet(onDismiss:()->Unit,onSelect:(Int)->Unit){
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(24.dp),color=Ivory,contentColor=Ink,shadowElevation=18.dp){
            Column(Modifier.widthIn(max=340.dp).verticalScroll(rememberScrollState()).padding(22.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){Text("Quick set",Modifier.weight(1f),fontFamily=Barlow,fontSize=28.sp);IconButton(onClick=onDismiss,modifier=Modifier.semantics{contentDescription="Close quick set"}){HardwareIconView(HardwareIcon.CLOSE,Modifier.size(20.dp))}}
                Text("Choose a duration.",Modifier.padding(bottom=18.dp),fontSize=13.sp,color=Color(0xFF58664D))
                listOf(listOf(1,3,5,10),listOf(15,20,30,60)).forEach{row->
                    Row(Modifier.fillMaxWidth().padding(bottom=8.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                        row.forEach{minutes->
                            Surface(Modifier.weight(1f).heightIn(min=58.dp).semantics{contentDescription="Set $minutes minutes"}.clickable{onSelect(minutes)},shape=RoundedCornerShape(8.dp),color=Color(0xFFDFE3D2)){
                                Column(Modifier.padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(minutes.toString(),fontFamily=Barlow,fontSize=26.sp,color=Ink);Text("min",fontSize=10.sp,color=Color(0xFF58664D))}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DurationPicker(initialMillis:Long,onHaptic:(HapticEvent)->Unit,onDismiss:()->Unit,onCommit:(Int)->Unit){
    val initialMinutes=(initialMillis/60_000.0).roundToInt().coerceIn(0,MAX_TIMER_MINUTES)
    var hoursText by rememberSaveable{mutableStateOf((initialMinutes/60).toString().padStart(2,'0'))}
    var minutesText by rememberSaveable{mutableStateOf((initialMinutes%60).toString().padStart(2,'0'))}
    var hourMode by rememberSaveable{mutableStateOf(true)}
    var keyboard by rememberSaveable{mutableStateOf(false)}
    val hours=hoursText.toIntOrNull()
    val minutes=minutesText.toIntOrNull()
    val validHours=hours!=null&&hours in 0..23
    val validMinutes=minutes!=null&&minutes in 0..59
    val valid=validHours&&validMinutes
    val windowHeightPx=LocalWindowInfo.current.containerSize.height
    val maxHeight=(with(LocalDensity.current){windowHeightPx.toDp()}-40.dp).coerceAtLeast(260.dp)
    fun selectValue(value:Int){
        if(hourMode){if(hours!=value){hoursText=value.toString().padStart(2,'0');onHaptic(HapticEvent.PICKER_TICK)}}
        else if(minutes!=value){minutesText=value.toString().padStart(2,'0');onHaptic(HapticEvent.PICKER_TICK)}
    }
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)){
        Surface(Modifier.padding(18.dp).widthIn(max=360.dp).fillMaxWidth().heightIn(max=maxHeight).imePadding(),shape=RoundedCornerShape(26.dp),color=Ivory,contentColor=Ink,shadowElevation=18.dp){
            Column(Modifier.padding(horizontal=24.dp,vertical=22.dp)){
                Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState())) {
                Text("Set duration",Modifier.padding(bottom=20.dp),fontFamily=Barlow,fontSize=30.sp,color=Ink)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.Top){
                    if(keyboard){
                        DurationField("Hours",hoursText,validHours,Modifier.weight(1f)){hoursText=it}
                        Text(":",Modifier.padding(top=10.dp),fontFamily=Barlow,fontSize=42.sp,color=Ink)
                        DurationField("Minutes",minutesText,validMinutes,Modifier.weight(1f)){minutesText=it}
                    }else{
                        PickerValue("Hours",(hours?:0).toString().padStart(2,'0'),hourMode,Modifier.weight(1f)){hourMode=true}
                        Text(":",Modifier.padding(top=6.dp),fontFamily=Barlow,fontSize=48.sp,color=Ink)
                        PickerValue("Minutes",(minutes?:0).toString().padStart(2,'0'),!hourMode,Modifier.weight(1f)){hourMode=false}
                    }
                }
                if(!keyboard){
                    ClockPicker(hourMode,if(hourMode)(hours?:0).coerceIn(0,23) else (minutes?:0).coerceIn(0,59),Modifier.fillMaxWidth().padding(top=22.dp).aspectRatio(1f),::selectValue,onFinished={if(hourMode)hourMode=false})
                    Text(if(hourMode)"Choose hours. Inner ring: 12–23." else "Choose minutes. Drag for exact values.",Modifier.fillMaxWidth().padding(top=12.dp),fontSize=12.sp,color=Color(0xFF58664D),textAlign=TextAlign.Center)
                }else{
                    Text("Hours 0–23 · Minutes 0–59",Modifier.fillMaxWidth().padding(top=22.dp,bottom=10.dp),fontSize=12.sp,color=Color(0xFF58664D),textAlign=TextAlign.Center)
                    if(!valid)Text(if(!validHours)"Enter hours from 0 to 23." else "Enter minutes from 0 to 59.",Modifier.padding(bottom=8.dp).semantics{liveRegion=LiveRegionMode.Polite},fontSize=12.sp,color=Color(0xFF9D3122))
                }
                }
                // Actions stay outside the scroll viewport, reachable at every window/font size.
                Row(Modifier.fillMaxWidth().padding(top=14.dp),verticalAlignment=Alignment.CenterVertically){
                    IconButton(onClick={keyboard=!keyboard},modifier=Modifier.semantics{contentDescription=if(keyboard)"Use clock dial" else "Type duration"}){HardwareIconView(if(keyboard)HardwareIcon.CLOCK else HardwareIcon.KEYBOARD,Modifier.size(24.dp))}
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick=onDismiss,colors=ButtonDefaults.textButtonColors(contentColor=Ink)){Text("Cancel")}
                    Button(onClick={if(valid){onHaptic(HapticEvent.PICKER_CONFIRM);onCommit(hours!!*60+minutes!!)}},enabled=valid,shape=RoundedCornerShape(8.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF465B37),contentColor=Color(0xFFF7F5E5))){Text("Set")}
                }
            }
        }
    }
}

@Composable
private fun DurationField(label:String,value:String,valid:Boolean,modifier:Modifier,onChange:(String)->Unit){
    OutlinedTextField(value=value,onValueChange={if(it.length<=2&&it.all(Char::isDigit))onChange(it)},modifier=modifier.semantics{contentDescription=label},singleLine=true,label={Text(label)},isError=!valid,textStyle=LocalTextStyle.current.copy(fontFamily=Barlow,fontSize=36.sp,textAlign=TextAlign.Center,color=Ink),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number,imeAction=ImeAction.Done),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Red,unfocusedBorderColor=Color(0xFFABB79B),focusedLabelColor=Ink,unfocusedLabelColor=Ink,cursorColor=Red,focusedContainerColor=Color(0xFFE2E6D5),unfocusedContainerColor=Color(0xFFE2E6D5)))
}

@Composable
private fun PickerValue(label:String,value:String,selected:Boolean,modifier:Modifier,onClick:()->Unit){
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally){
        Box(Modifier.fillMaxWidth().heightIn(min=76.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(if(selected)listOf(Color(0xFFAA4631),Color(0xFF913521)) else listOf(Color(0xFFDCE0CB),Color(0xFFE6E8D8)))).selectable(selected,role=Role.Button,onClick=onClick).semantics{contentDescription="Select ${label.lowercase()}";stateDescription=value}.padding(vertical=5.dp),contentAlignment=Alignment.Center){Text(value,fontFamily=Barlow,fontSize=51.sp,color=if(selected)Color(0xFFFFF3DF) else Color(0xFF526044))}
        Text(label.uppercase(),Modifier.padding(top=6.dp),fontSize=11.sp,letterSpacing=1.3.sp,color=Color(0xFF515A46))
    }
}

@Composable
private fun ClockPicker(hours:Boolean,value:Int,modifier:Modifier,onValue:(Int)->Unit,onFinished:()->Unit){
    val context=LocalContext.current
    val font=remember(context){context.resources.getFont(R.font.barlow_condensed)}
    val latestValue by rememberUpdatedState(value)
    val latestOnValue by rememberUpdatedState(onValue)
    val latestFinished by rememberUpdatedState(onFinished)
    Canvas(modifier.clip(androidx.compose.foundation.shape.CircleShape).background(Brush.linearGradient(listOf(Color(0xFFDFE3D1),Color(0xFFECECDF))))
        .semantics{contentDescription=if(hours)"Hours dial" else "Minutes dial";stateDescription=value.toString();progressBarRangeInfo=ProgressBarRangeInfo(value.toFloat(),0f..if(hours)23f else 59f);setProgress{onValue(it.roundToInt().coerceIn(0,if(hours)23 else 59));true};customActions=listOf(CustomAccessibilityAction("Increase"){onValue((value+1).coerceAtMost(if(hours)23 else 59));true},CustomAccessibilityAction("Decrease"){onValue((value-1).coerceAtLeast(0));true})}
        .onKeyEvent { event ->
            if(event.type!=KeyEventType.KeyDown)false else {
                val step=if(event.isShiftPressed)5 else 1
                when(event.key){Key.DirectionRight,Key.DirectionUp->{onValue((value+step).coerceAtMost(if(hours)23 else 59));true};Key.DirectionLeft,Key.DirectionDown->{onValue((value-step).coerceAtLeast(0));true};Key.MoveHome->{onValue(0);true};Key.MoveEnd->{onValue(if(hours)23 else 59);true};Key.Enter->{onFinished();true};else->false}
            }
        }.focusable()
        .pointerInput(hours){
            fun choose(position:Offset){
                val cx=size.width/2f;val cy=size.height/2f;val dx=position.x-cx;val dy=position.y-cy
                val distance=sqrt(dx*dx+dy*dy)
                if(distance<size.width*.12f)return
                val angle=(atan2(dx.toDouble(),-dy.toDouble())+2*PI)%(2*PI)
                val selected=if(hours)((angle*12/(2*PI)).roundToInt()%12)+(if(distance<size.width*.33f)12 else 0) else (angle*60/(2*PI)).roundToInt()%60
                latestOnValue(selected)
            }
            awaitEachGesture{
                val down=awaitFirstDown();choose(down.position);down.consume();var cancelled=false
                while(true){val event=awaitPointerEvent();if(event.changes.count{it.pressed}>1){cancelled=true;break};val change=event.changes.firstOrNull{it.id==down.id}?:break;if(!change.pressed)break;choose(change.position);change.consume()}
                if(!cancelled)latestFinished()
            }
        }){
        drawIntoCanvas{wrapper->
            val c=wrapper.nativeCanvas;c.save();c.scale(size.width/300f,size.height/300f)
            val cx=150f;val cy=150f;val outer=121.5f;val inner=76.5f
            val a=(if(hours)value%12*30 else value*6)*PI/180
            val radius=if(hours&&value>=12)inner else outer
            val x=cx+sin(a).toFloat()*radius;val y=cy-cos(a).toFloat()*radius
            val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=0xFF9B3B29.toInt();strokeWidth=3.6f;strokeCap=Paint.Cap.ROUND}
            c.drawLine(cx,cy,x,y,p);c.drawCircle(x,y,21.3f,p);c.drawCircle(cx,cy,5.1f,p)
            if(!hours){p.color=0xFF89967A.toInt();for(i in 0 until 60){val angle=i*PI/30;val r=138f;c.drawLine(cx+sin(angle).toFloat()*r,cy-cos(angle).toFloat()*r,cx+sin(angle).toFloat()*142f,cy-cos(angle).toFloat()*142f,p.apply{strokeWidth=if(i%5==0)1.5f else .8f})}}
            val textPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=font;textAlign=Paint.Align.CENTER}
            val values=if(hours)(0..23).toList() else (0..55 step 5).toList()+if(value%5!=0)listOf(value) else emptyList()
            values.forEach{n->
                val difference=min(abs(n-value),60-abs(n-value))
                if(!hours&&n%5==0&&value%5!=0&&difference<=2)return@forEach
                val angle=(if(hours)n%12*30 else n*6)*PI/180
                val r=if(hours&&n>=12)inner else outer
                textPaint.color=if(n==value)0xFFFFF3DF.toInt() else if(hours&&n>=12)0xFF5A654E.toInt() else 0xFF394333.toInt()
                textPaint.textSize=if(hours&&n>=12)20f else 25f
                c.drawText(if(hours)n.toString() else n.toString().padStart(2,'0'),cx+sin(angle).toFloat()*r,cy-cos(angle).toFloat()*r-(textPaint.ascent()+textPaint.descent())/2,textPaint)
            }
            c.restore()
        }
    }
}
