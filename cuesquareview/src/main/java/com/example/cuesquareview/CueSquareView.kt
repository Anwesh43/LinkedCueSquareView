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
    translate(w / 2, h)
    drawRect(RectF(-barW / 2, -h * 0.5f * sf1, barW / 2, 0f), paint)
    restore()
    save()
    translate(0f, h / 2 - r)
    drawRect(RectF(0f, -barW / 2, (w * 0.5f - r + r * 0.05f) * sf3,barW / 2), paint)
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

class CueSquareView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CSNode(var i : Int, val state : State = State()) {

        private var next : CSNode? = null
        private var prev : CSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (this.i < colors.size - 1) {
                next = CSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCSNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CSNode {
            var curr : CSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CueSquare(var i : Int) {

        private var curr : CSNode = CSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CueSquareView) {

        private val animator : Animator = Animator(view)
        private val cs : CueSquare = CueSquare(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            cs.draw(canvas, paint)
            animator.animate {
                cs.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : CueSquareView {
            val view : CueSquareView = CueSquareView(activity)
            activity.setContentView(view)
            return view
        }
    }
}