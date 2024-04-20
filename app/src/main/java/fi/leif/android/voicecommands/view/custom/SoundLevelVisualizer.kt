package fi.leif.android.voicecommands.view.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class SoundLevelVisualizer(context: Context, attrs: AttributeSet?) : View(context, attrs){

    companion object {
        const val DEFAULT_MAX_LEVEL = 10f

        private val COLOR1 = Color.argb(255,88,233,228)
        private val COLOR2 = Color.argb(255,0,255,0)
        private val COLOR3 = Color.argb(255,255,0,0)

        private const val DOT_RADIUS_DPI = 0.06f

        private const val DOT_MIN_RADIUS_DPI = 0.02f
        private const val DOT_GAP_DPI = 0.05f
        private const val MIN_DOTS_DIV = 2 // 1..n - greater number => higher minimum height

        private const val LOOP_DELAY_MS = 100L
        private const val FADE_DECREASE = 20
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 0f
        style = Paint.Style.FILL
    }

    private val _cache = LevelsCache(DEFAULT_MAX_LEVEL)
    class LevelsCache(var maxLevel: Float) {
        private var _sum = 0f
        private var _count = 0

        fun addLevel(level: Float) {
            // Filter out numbers out of scope
            val l = if(level<0f) 0f else if(level>maxLevel) maxLevel else level
            _sum += l
            _count++
        }

        fun avg(): Float {
            return if(_count == 0) 0f else _sum/_count
        }

        fun reset() {
            _sum = 0f
            _count = 0
        }
    }
    var level: Float = 0f
        set(value) {
            field = value
            _cache.addLevel(field)

        }
        get(): Float {
            return _cache.avg()
        }

    var maxLevel = DEFAULT_MAX_LEVEL
        set(value) {
            field = value
            _cache.maxLevel = value
        }

    private val animator = ValueAnimator.ofInt(0, 1).apply {
        repeatCount = ValueAnimator.INFINITE
        duration = LOOP_DELAY_MS
        addUpdateListener {
            if (level > 0f && width > 0 && height > 0) {
                shapes.add(Shape(getColumnsBitmap(level)))
            }
            _cache.reset()
            invalidate()
        }
    }

    fun stop() {
        animator.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    data class Shape(val bitmap: Bitmap, var alpha: Int = 255)
    private val shapes = mutableListOf<Shape>()

    private lateinit var colors: List<Int>
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val maxHeight = height
        val colorsNr = (maxHeight/ dpi2px(DOT_RADIUS_DPI))
        colors = generateColorGradient(listOf(COLOR1, COLOR2, COLOR3), colorsNr)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBitmaps(canvas)
    }

    private val alphaPaint: Paint = Paint()
    private fun drawBitmaps(canvas:Canvas) {
        val iterator = shapes.iterator()
        while(iterator.hasNext()) {
            val shape = iterator.next()
            if(shape.alpha > 0) {
                alphaPaint.alpha = shape.alpha
                canvas.drawBitmap(shape.bitmap, 0f, 0f, alphaPaint)
                if(iterator.hasNext() || shapes.size == 1) {
                    shape.alpha -= FADE_DECREASE
                }
            } else {
                iterator.remove()
            }
        }
    }

    private fun getColumnsBitmap(level: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val dotRadius = dpi2px(DOT_RADIUS_DPI)
        val dotGap = dpi2px(DOT_GAP_DPI)
        val y = height - dotRadius

        val maxH: Float = (level/maxLevel) * height
        val maxDots = (maxH/dotRadius).toInt()
        val minDots = maxDots/MIN_DOTS_DIV

        for(x in dotRadius .. width step (dotRadius + dotGap)) {
            if(maxDots-minDots>0) {
                val h = Random.nextInt(maxDots - minDots) + minDots
                drawColumn(canvas, x, y, h)
            }
        }
        return bitmap
    }

    private fun drawColumn(canvas: Canvas, x: Int, startY: Int, h: Int) {
        var y = startY
        val dotRadius = dpi2px(DOT_RADIUS_DPI)
        val dotMinRadius = dpi2px(DOT_MIN_RADIUS_DPI)

        for(i in 0 until h) {
            val factor = (h-i)/h.toFloat()
            val size = ((dotRadius-dotMinRadius) * factor) + dotMinRadius
            paint.color = colors[i]
            canvas.drawCircle(x.toFloat(),y.toFloat(),size, paint)
            y -= dotRadius
        }
    }

    private val dpi: Int = context.resources.displayMetrics.densityDpi
    private fun dpi2px(factor: Float): Int {
        return (factor * dpi).toInt()
    }

    private fun generateColorGradient(colors: List<Int>, numberOfColors: Int): List<Int> {
        require(numberOfColors >= 2) { "Number of colors must be at least 2" }

        val colorList = mutableListOf<Int>()

        // Calculate step size for color interpolation
        val step = 1f / (numberOfColors - 1)

        for (i in 0 until numberOfColors) {
            // Calculate interpolation factor for current color
            val t = i * step

            // Interpolate between colors based on interpolation factor
            val interpolatedColor = interpolateColors(colors, t)

            colorList.add(interpolatedColor)
        }

        return colorList
    }

    private fun interpolateColors(colors: List<Int>, t: Float): Int {
        val segment = t * (colors.size - 1)
        val index = segment.toInt()
        val remainder = segment - index

        val color1 = colors[index]
        val color2 = colors[minOf(index + 1, colors.size - 1)]

        return Color.argb(
            interpolate(Color.alpha(color1), Color.alpha(color2), remainder).toInt(),
            interpolate(Color.red(color1), Color.red(color2), remainder).toInt(),
            interpolate(Color.green(color1), Color.green(color2), remainder).toInt(),
            interpolate(Color.blue(color1), Color.blue(color2), remainder).toInt()
        )
    }

    private fun interpolate(start: Int, end: Int, t: Float): Float {
        return start + (end - start) * t
    }
}