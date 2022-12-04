package id.psw.vshlauncher.views

import android.graphics.*
import android.os.Build
import id.psw.vshlauncher.FittingMode
import id.psw.vshlauncher.select
import id.psw.vshlauncher.toLerp

private val drawBitmapFitRectFBuffer = RectF()
fun Canvas.drawBitmap(bm:Bitmap, src: Rect?, dst: RectF, paint: Paint?, fitMode:FittingMode, anchorX:Float = 0.5f, anchorY:Float = 0.5f){
    if(!bm.isRecycled){
        if (fitMode == FittingMode.STRETCH) {
            drawBitmap(bm, src, dst, paint)
        } else {
            val w = src?.width() ?: bm.width
            val h = src?.height() ?: bm.height

            val sx = dst.width() / w
            val sy = dst.height() / h
            val sc = (fitMode == FittingMode.FIT).select(
                (sx <= sy).select(sx, sy),
                (sx >= sy).select(sx, sy),
            )

            val sw = w * sc
            val sh = h * sc
            val left = anchorX.toLerp(dst.left, dst.right - sw)
            val top = anchorY.toLerp(dst.top, dst.bottom - sh)
            val right = left + sw
            val bottom = top + sh

            drawBitmapFitRectFBuffer.set(left, top, right, bottom)
            drawBitmap(bm, src, drawBitmapFitRectFBuffer, paint)
        }
    }
}

fun Paint.removeShadowLayer() = setShadowLayer(0.0f, 0.0f, 0.0f, Color.TRANSPARENT)

private var extCanvasDrawTextRectFBuffer = RectF()
private var extCanvasDrawTextRectBuffer = Rect()

fun Canvas.drawText(text:String, x:Float, y:Float, paint: Paint, yOffset:Float, useTextBound : Boolean = false){
    val h = if(useTextBound) {
        paint.getTextBounds(text, 0, text.length, extCanvasDrawTextRectBuffer)
        extCanvasDrawTextRectBuffer.height().toFloat()
    }else paint.textSize
    drawText(text, x, y + (yOffset * h), paint)
}

fun Canvas.drawRoundRect(base:RectF, r:Float, paint:Paint){
    drawRoundRect(base, r, r, paint)
}

fun Path.addRoundRect(x1:Float,y1:Float,x2:Float,y2:Float,r:Float,dir:Path.Direction){
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
        addRoundRect(x1,y1,x2,y2,r,r,dir)
    }else{
        moveTo(x1 + r, y1)
        lineTo(x2 - r, y1)
        quadTo(x2, y1, x2,y1 + r)
        lineTo(x2, y2 - r)
        quadTo(x2,y2,x2 - r,y2)
        lineTo(x1 + r,y2)
        quadTo(x1,y2,x1,y2 - r)
        lineTo(x1, y1 + r)
        quadTo(x1,y1, x1 + r,y1)
        close()
    }
}

fun Paint.setColorAndSize(color:Int, size: Float, align: Paint.Align){
    textSize = size
    this.color = color
    this.textAlign = align
}

fun Paint.wrapText(source:String, maxWidth:Float) : String {
    val msb = StringBuilder()
    val line = StringBuilder()
    for(word in source.split(' ')){
        if(word.startsWith("\n")){
            msb.appendLine(line.toString())
            line.clear()
            line.append(word.replace("\n","")).append(' ')
        }else if(word.endsWith("\n")){
            line.append(word.replace("\n","")).append(' ')
            msb.appendLine(line.toString())
            line.clear()
        }else{
            if(measureText("$line $word") > maxWidth){
                msb.appendLine(line.toString())
                line.clear()
            }
            line.append(word).append(' ')
        }
    }
    msb.appendLine(line.toString())
    return msb.toString().trimEnd('\n')
}