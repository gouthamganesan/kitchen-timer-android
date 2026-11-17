package dev.goutham.kitchentimer.ui

import android.graphics.Canvas as NativeCanvas
import android.graphics.Color as NativeColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import dev.goutham.kitchentimer.R
import kotlin.math.*

internal val Ivory = Color(0xFFF0EDDF)
internal val Ink = Color(0xFF37412F)
internal val Red = Color(0xFFAD402D)
internal val Casing = Color(0xFF292E2B)
internal val Sage = Color(0xFF5B704A)

internal fun argb(hex: Long) = hex.toInt()
private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
private fun linear(vararg colors: Int, x0: Float = 0f, y0: Float = 0f, x1: Float = 360f, y1: Float = 360f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    shader = LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
}
private fun NativeCanvas.roundRect(bounds: RectF, radius: Float, p: Paint) = drawRoundRect(bounds, radius, radius, p)
private fun layerColor(layer: Int, darkFocus: Boolean): Int {
    val light = if (darkFocus) 36 + 32 * exp(-.27 * (layer - 1)) else 20 + 28 * exp(-.27 * (layer - 1))
    val h = if (darkFocus) 30f else 7f
    val l = light.toFloat() / 100f
    val s = .64f
    val chroma = (1f - abs(2f * l - 1f)) * s
    val x = chroma * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - chroma / 2f
    return NativeColor.rgb(((chroma + m) * 255).roundToInt(), ((x + m) * 255).roundToInt(), (m * 255).roundToInt())
}

@Composable
internal fun DialDrawing(modifier: Modifier, remainingMillis: Long, focus: Boolean, dark: Boolean) {
    val context = LocalContext.current
    val typeface = remember(context) { context.resources.getFont(R.font.barlow_condensed) }
    Canvas(modifier) {
        drawIntoCanvas { wrapper ->
            val c = wrapper.nativeCanvas
            c.save()
            c.scale(size.width / 360f, size.height / 360f)
            drawDial(c, remainingMillis, focus, dark, typeface)
            c.restore()
        }
    }
}

private fun drawDial(c: NativeCanvas, millis: Long, focus: Boolean, dark: Boolean, font: Typeface) {
    val darkFocus = dark && focus
    if (!focus) {
        val shadow = paint(0xFF1C211B.toInt()).apply { setShadowLayer(6f, 2f, 6f, 0x880E150F.toInt()) }
        c.roundRect(RectF(2f, 1f, 358f, 355f), 45f, shadow)
        c.roundRect(RectF(1f, 0f, 359f, 355f), 44f, linear(0xFF77806C.toInt(), 0xFF20261F.toInt(), 0xFF161D17.toInt()))
        c.roundRect(RectF(3f, 3f, 357f, 352f), 43f, linear(0xFF20271F.toInt(), 0xFF434B3D.toInt(), 0xFF161D15.toInt()))
        c.roundRect(RectF(15f, 15f, 345f, 341f), 35f, linear(0xFF989D87.toInt(), if(dark) 0xFFF3EBBF.toInt() else 0xFFF4F2DE.toInt(), 0xFFD6D4BE.toInt()))
        c.roundRect(RectF(24f, 25f, 336f, 330f), 27f, linear(if(dark) 0xFFEAE3BC.toInt() else 0xFFF6F4E7.toInt(), if(dark) 0xFFF8F2DA.toInt() else 0xFFECE9D9.toInt()))
        // The bezel shades the paper. Light emerges from two narrow diffuser edges.
        c.roundRect(RectF(24f, 25f, 336f, 330f), 27f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 2f; color = 0x497D8266
        })
        if (dark) {
            c.save(); c.clipRoundRectCompat(RectF(25f, 26f, 335f, 329f), 26f)
            c.drawCircle(25f, 265f, 150f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = RadialGradient(25f,265f,150f,intArrayOf(0xFFFFFEEB.toInt(),0xAFFFF5C2.toInt(),0x00FFF3B9),floatArrayOf(0f,.16f,1f),Shader.TileMode.CLAMP) })
            c.drawCircle(337f, 264f, 118f, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = RadialGradient(337f,264f,118f,intArrayOf(0xFFFFFDED.toInt(),0x6FFFF3B7,0x00FFF1AF),floatArrayOf(0f,.15f,1f),Shader.TileMode.CLAMP) })
            c.drawLine(26f,242f,26f,284f,paint(0xD9FFFFEB.toInt()).apply{strokeWidth=2f})
            c.drawLine(334f,242f,334f,284f,paint(0x99FFFCE5.toInt()).apply{strokeWidth=1.5f})
            c.restore()
        }
    }
    // The lip casts a soft inset contact shadow onto the paper, separate from emitted light.
    if (!focus) {
        c.save()
        c.clipRoundRectCompat(RectF(24f, 25f, 336f, 330f), 27f)
        c.roundRect(RectF(23f, 23f, 337f, 331f), 28f, paint(0x55757C61).apply {
            style = Paint.Style.STROKE; strokeWidth = 2f
            setShadowLayer(4.5f, 1f, 2.5f, 0x7770785B)
        })
        c.restore()
        // A restrained soft highlight describes the curved rubber shoulder.
        c.roundRect(RectF(3f, 3f, 357f, 352f), 43f, linear(0x557A8572,0x0020271F,0x15161D15,x0=5f,y0=5f,x1=250f,y1=220f).apply {
            style=Paint.Style.STROKE;strokeWidth=2f
        })
    }
    val center = 180f
    val discRadius = if (focus) 111f else 101f
    val seconds = ceil(millis.coerceAtLeast(0) / 1000.0).toLong()
    val hours = (seconds / 3600).toInt()
    val fraction = (seconds % 3600) / 3600f
    val angle = fraction * 360f
    val paperDisc = if (darkFocus) 0xFF2B3937.toInt() else 0xFFECE9D9.toInt()
    c.drawCircle(center, center, discRadius, paint(paperDisc))
    if (hours > 0) c.drawCircle(center,center,discRadius,paint(layerColor(hours,darkFocus)))
    if (fraction > 0f) c.drawArc(RectF(center-discRadius,center-discRadius,center+discRadius,center+discRadius),-90f,angle,true,paint(layerColor(hours+1,darkFocus)))
    c.drawCircle(center, center, discRadius, linear(0x1AFFFFFF,0x00FFFFFF,0x120F0000,x0=75f,y0=75f,x1=285f,y1=285f))
    val scaleColor = if(darkFocus) 0xFFD3DED4.toInt() else 0xFF363D30.toInt()
    val tickPaint = paint(scaleColor)
    val textPaint = paint(scaleColor).apply { typeface=font; textAlign=Paint.Align.CENTER; textSize=23f }
    val innerTick = if(focus) 126f else 114f
    val outerTick = if(focus) 140f else 127f
    val textRadius = if(focus) 155f else 140f
    for (i in 0 until 60) {
        val a = i * PI / 30.0
        val major = i%5 == 0
        tickPaint.strokeWidth = if(major) 2.2f else 1.05f
        val r0=if(major)innerTick else outerTick-8f
        c.drawLine(center+sin(a).toFloat()*r0,center-cos(a).toFloat()*r0,center+sin(a).toFloat()*outerTick,center-cos(a).toFloat()*outerTick,tickPaint)
        if(major) {
            val label = if(i==0)"60" else i.toString()
            val baseline = center-cos(a).toFloat()*textRadius-(textPaint.ascent()+textPaint.descent())/2f
            c.drawText(label,center+sin(a).toFloat()*textRadius,baseline,textPaint)
        }
    }
    val a=angle*PI/180.0
    val needlePaint=paint(if(darkFocus)0xFFE7E6CE.toInt() else 0xFFF4F1DD.toInt()).apply {strokeWidth=4f;strokeCap=Paint.Cap.BUTT}
    c.drawLine(center,center,center+sin(a).toFloat()*discRadius,center-cos(a).toFloat()*discRadius,needlePaint)
    val knobR=26f
    c.drawCircle(center+1f,center+3f,knobR-1f,paint(0x19000000).apply{setShadowLayer(5f,3f,6f,if(darkFocus)0x66071516 else 0x55312E26)})
    c.drawCircle(center,center,knobR,paint(if(darkFocus)0xFF30443F.toInt() else 0xFFB7B5A0.toInt()))
    c.drawCircle(center,center-1f,knobR-1f,linear(if(darkFocus)0xFFB7C7B7.toInt() else if(dark)0xFFFFFFEF.toInt() else 0xFFFFFEF0.toInt(),if(darkFocus)0xFF778F84.toInt() else if(dark)0xFFD5D1AD.toInt() else 0xFFD3D1BC.toInt(),x0=160f,y0=150f,x1=205f,y1=212f))
    c.drawCircle(center,center-1f,knobR-1f,Paint(Paint.ANTI_ALIAS_FLAG).apply { shader=RadialGradient(168f,163f,37f,intArrayOf(0x70FFFFFF,0x00FFFFFF),null,Shader.TileMode.CLAMP) })
    c.drawArc(RectF(155f,154f,205f,204f),215f,105f,false,paint(if(darkFocus)0x70DDE5D1 else 0x80FFFFFF.toInt()).apply {style=Paint.Style.STROKE;strokeWidth=.7f;setShadowLayer(1f,0f,1f,0x40FFFFFF)})
}

private fun NativeCanvas.clipRoundRectCompat(rect:RectF,r:Float) { clipPath(Path().apply{addRoundRect(rect,r,r,Path.Direction.CW)}) }

@Composable
internal fun PushButtonDrawing(modifier: Modifier, primary: Boolean, pressed: Float, lit: Float, icon: HardwareIcon, enabled: Boolean) {
    Canvas(modifier) { drawIntoCanvas { wrapper ->
        val c=wrapper.nativeCanvas; c.save(); c.scale(size.width/100f,size.height/100f)
        c.drawCircle(50f,50f,48f,linear(0xFF080E09.toInt(),0xFF1B241B.toInt(),0xFF59614E.toInt(),x1=100f,y1=100f))
        c.drawCircle(51f,56f,40f,paint(0x7710170F).apply{setShadowLayer(4f,2f,3f,0x9910170F.toInt())})
        c.drawCircle(50f,54f,40f,linear(if(primary)0xFF7D2418.toInt() else 0xFF272E26.toInt(),if(primary)0xFFAC3422.toInt() else 0xFF454C3D.toInt(),if(primary)0xFF702419.toInt() else 0xFF272F25.toInt(),x1=100f,y1=50f))
        val cy=45f+7f*pressed
        c.drawCircle(50f,cy,40f,linear(if(primary)0xFFE87252.toInt() else 0xFF858D76.toInt(),if(primary)0xFFCB452D.toInt() else 0xFF58644D.toInt(),if(primary)0xFFAD3221.toInt() else 0xFF414B39.toInt(),x0=20f,y0=5f,x1=80f,y1=90f))
        c.drawCircle(50f,cy,39.5f,Paint(Paint.ANTI_ALIAS_FLAG).apply {shader=RadialGradient(30f,cy-29f,65f,intArrayOf(if(primary)0x60FFC2A2 else 0x50D8E5B8,0x00FFFFFF),null,Shader.TileMode.CLAMP)})
        c.drawArc(RectF(11f,cy-39f,89f,cy+39f),210f,110f,false,paint(if(primary)0x80FFB695.toInt() else 0x70BDC6A6).apply{style=Paint.Style.STROKE;strokeWidth=.8f;setShadowLayer(1f,0f,1f,0x40FFFFFF)})
        c.drawArc(RectF(11f,cy-39f,89f,cy+39f),25f,120f,false,paint(if(primary)0x30842718 else 0x30232E1F).apply{style=Paint.Style.STROKE;strokeWidth=1f})
        if(lit>.001f)c.drawCircle(50f,cy+8f,36f,Paint(Paint.ANTI_ALIAS_FLAG).apply {shader=RadialGradient(50f,cy+8f,36f,intArrayOf(if(primary)0x70FFEA9B else 0x60D9F68D,0x00FFEA9B),null,Shader.TileMode.CLAMP);alpha=(lit*255).roundToInt()})
        c.save(); c.translate(36f,cy-14f); c.scale(28f/24f,28f/24f)
        drawHardwareIcon(c,icon,if(enabled)if(lit>.2f)0xFFFFFFE0.toInt() else 0xFFF2EEDC.toInt() else 0x99746F5F.toInt(),2.1f)
        c.restore();c.restore()
    } }
}

internal enum class HardwareIcon { PLAY, PAUSE, STOP, RESET, SOUND, VIBRATE, EDIT, CLOSE, KEYBOARD, CLOCK, CHECK }

@Composable
internal fun HardwareIconView(icon: HardwareIcon, modifier: Modifier, tint: Color=Ink) {
    Canvas(modifier) {drawIntoCanvas { wrapper ->
        val c=wrapper.nativeCanvas;c.save();c.scale(size.width/24f,size.height/24f);drawHardwareIcon(c,icon,tint.toArgbCompat(),1.8f);c.restore()
    }}
}
private fun Color.toArgbCompat():Int = NativeColor.argb((alpha*255).roundToInt(),(red*255).roundToInt(),(green*255).roundToInt(),(blue*255).roundToInt())

private fun drawHardwareIcon(c:NativeCanvas,icon:HardwareIcon,color:Int,width:Float) {
    val p=paint(color).apply {style=Paint.Style.STROKE;strokeWidth=width;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND}
    fun line(x:Float,y:Float,x1:Float,y1:Float)=c.drawLine(x,y,x1,y1,p)
    when(icon){
        HardwareIcon.PLAY -> { p.style=Paint.Style.FILL; c.drawPath(Path().apply{moveTo(7f,4f);lineTo(20f,12f);lineTo(7f,20f);close()},p) }
        HardwareIcon.PAUSE -> {p.style=Paint.Style.FILL;c.roundRect(RectF(5f,4f,9f,20f),1f,p);c.roundRect(RectF(15f,4f,19f,20f),1f,p)}
        HardwareIcon.STOP -> {p.style=Paint.Style.FILL;c.roundRect(RectF(5f,5f,19f,19f),2f,p)}
        HardwareIcon.RESET -> {c.drawArc(RectF(4f,4f,21f,21f),215f,300f,false,p);line(3f,3f,3f,10f);line(3f,10f,10f,10f)}
        HardwareIcon.SOUND -> {c.drawPath(Path().apply{moveTo(3f,9f);lineTo(7f,9f);lineTo(12f,5f);lineTo(12f,19f);lineTo(7f,15f);lineTo(3f,15f);close()},p);c.drawArc(RectF(7f,5f,20f,19f),-50f,100f,false,p);c.drawArc(RectF(6f,2f,25f,22f),-45f,90f,false,p)}
        HardwareIcon.VIBRATE -> {c.roundRect(RectF(8f,4f,16f,20f),1.4f,p);line(4f,7f,2f,10f);line(2f,10f,4f,13f);line(4f,13f,2f,16f);line(20f,7f,22f,10f);line(22f,10f,20f,13f);line(20f,13f,22f,16f)}
        HardwareIcon.EDIT -> {c.drawPath(Path().apply{moveTo(5f,15f);lineTo(15f,5f);lineTo(19f,9f);lineTo(9f,19f);lineTo(4f,20f);close()},p);line(13f,7f,17f,11f)}
        HardwareIcon.CLOSE -> {line(6f,6f,18f,18f);line(6f,18f,18f,6f)}
        HardwareIcon.KEYBOARD -> {c.roundRect(RectF(2f,4f,22f,20f),2f,p);for(y in listOf(8f,12f))for(x in listOf(6f,10f,14f,18f))c.drawPoint(x,y,p);line(7f,16f,17f,16f)}
        HardwareIcon.CLOCK -> {c.drawCircle(12f,12f,9f,p);line(12f,6f,12f,12f);line(12f,12f,16f,15f)}
        HardwareIcon.CHECK -> {line(4f,12f,9f,17f);line(9f,17f,20f,6f)}
    }
}
