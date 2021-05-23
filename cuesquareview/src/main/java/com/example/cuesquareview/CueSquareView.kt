package com.example.cuesquareview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Canvas
import android.app.Activity
import android.content.Context

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#311B92",
    "#00C853",
    "#00C853",
    "#C51162"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 4
val scGap : Float = 0.02f / parts
val barWFactor : Float = 13.2f
val rFactor : Float = 9.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawCueSquare(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf: Float = scale.sinify()
    val sf1: Float = sf.divideScale(0, parts)
    val sf2: Float = sf.divideScale(1, parts)
    val sf3: Float = sf.divideScale(2, parts)
    val sf4: Float = sf.divideScale(3, parts)
    val r: Float = Math.min(w, h) / rFactor
    val barW : Float = Math.min(w, h) / barWFactor
    save()
    save()
    translate(w / 2, 0f)
    drawRect(RectF(-barW / 2, -h * 0.5f * sf1, barW / 2, 0f), paint)
    restore()
    save()
    translate(0f, h / 2 - r)
    drawRect(RectF(0f, -barW / 2, w * 0.5f * sf3,barW / 2), paint)
    restore()
    drawCircle(w / 2 + (w / 2 + r) * sf4, -r + (h /2) * sf2, r, paint)
    restore()
}

fun Canvas.drawCSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawCueSquare(scale, w, h, paint)
}
