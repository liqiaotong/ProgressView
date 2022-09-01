package com.liqiaotong.lib

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import kotlin.math.abs
import kotlin.math.roundToInt


open class AnProgressView : View {

    private val defaultColor: Int = Color.parseColor("#1D9AF8")
    private var numberColor: Int = Color.parseColor("#FFFFFF")
    private var fixedPointColor: Int = defaultColor
    private var pointColor: Int = defaultColor
    private var lineColor: Int = defaultColor
    private var number: String? = "0"
    private var linePaint: Paint? = null
    private var isShowNumber: Boolean = false
    private var numberSize: Float = 30f
    private var progressF: Float = 0f
    private var pressProgress: Float = 0f
    private var pressDuration: Long = 150L
    private var valueAnimator: ValueAnimator? = null
    private var interpolator = DecelerateInterpolator()
    private var fixedPointPaint: Paint? = null
    private var fixedPointSize: Float = 45f
    private var fixedPointCount: Int = 2
    private var pointPaint: Paint? = null
    private var numberPaint: Paint? = null
    private var pointSize: Float = 90f
    private var lineHeight = 10f
    private val numberRect = Rect()
    private var listener: OnAnProgressViewListener? = null

    fun setOnAnProgressViewListener(listener: OnAnProgressViewListener?) {
        this.listener = listener
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initResource(context, attrs, defStyleAttr)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)

    private fun initResource(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        context?.let {
            var a = context.obtainStyledAttributes(attrs, R.styleable.AnProgressView, defStyleAttr, 0)
            lineColor = a.getColor(R.styleable.AnProgressView_apv_lineColor, defaultColor)
            fixedPointColor = a.getColor(R.styleable.AnProgressView_apv_fixedPointColor, defaultColor)
            pointColor = a.getColor(R.styleable.AnProgressView_apv_pointColor, defaultColor)
            numberColor = a.getColor(R.styleable.AnProgressView_apv_numberColor, Color.parseColor("#FFFFFF"))
            numberSize = a.getDimension(R.styleable.AnProgressView_apv_numberSize, 30f)
            isShowNumber = a.getBoolean(R.styleable.AnProgressView_apv_isShowNumber, false)
            lineHeight = a.getDimension(R.styleable.AnProgressView_apv_lineHeight, 10f)
            fixedPointSize = a.getDimension(R.styleable.AnProgressView_apv_fixedPointSize, 45f)
            pointSize = a.getDimension(R.styleable.AnProgressView_apv_pointSize, 90f)
            fixedPointCount = a.getInt(R.styleable.AnProgressView_apv_fixedPointCount, 2)?.let { if (it < 2) 2 else it }
            a.recycle()
        }

        initPaint()
        updateValue()
        invalidate()
    }

    private fun initPaint() {
        linePaint = Paint()
        linePaint?.isAntiAlias = true
        linePaint?.color = defaultColor

        fixedPointPaint = Paint()
        fixedPointPaint?.isAntiAlias = true
        fixedPointPaint?.color = defaultColor

        pointPaint = Paint()
        pointPaint?.isAntiAlias = true
        pointPaint?.color = defaultColor

        numberPaint = Paint()
        numberPaint?.isAntiAlias = true
        numberPaint?.color = defaultColor
        numberPaint?.textSize = numberSize
        numberPaint?.color = numberColor
        numberPaint?.textAlign = Paint.Align.CENTER
    }

    fun setPressDuration(duration: Long) {
        this.pressDuration = duration
    }

    private fun startPressAnimation(isPress: Boolean = true, pressDuration: Long? = null) {
        if (this.isPress == isPress) return
        this.isPress = isPress
        valueAnimator?.cancel()
        valueAnimator = ValueAnimator.ofFloat(pressProgress, if (isPress) 1f else 0f)
        valueAnimator?.duration = pressDuration ?: this.pressDuration
        valueAnimator?.addUpdateListener {
            pressProgress = it.animatedValue as Float
            invalidate()
        }
        valueAnimator?.interpolator = interpolator
        valueAnimator?.start()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateRectF(width, height)
    }

    private var rectF: RectF? = null

    private fun updateRectF(width: Int? = null, height: Int? = null) {
        val vWidth = width ?: this.width
        val vHeight = height ?: this.height
        rectF = RectF(0f, 0f, vWidth?.toFloat(), vHeight?.toFloat())
        updateValue()
    }

    private var cwidth: Float = 0f
    private var cheight: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var maxPointSize: Float = 0f
    private var lineHeighC: Float = 0f
    private var lineStartX: Float = 0f
    private var lineEndX: Float = 0f
    private var lineLength: Float = 0f
    private var pointSizeRC: Float = 0f

    private fun updateValue() {
        cwidth = rectF?.width() ?: width?.toFloat()
        cheight = rectF?.height() ?: height?.toFloat()
        centerX = cwidth * 0.5f
        centerY = cheight * 0.5f
        maxPointSize = if (pointSize > fixedPointSize) pointSize else fixedPointSize
        lineHeighC = lineHeight * 0.5f
        lineStartX = maxPointSize * 0.5f
        lineEndX = cwidth - maxPointSize * 0.5f
        lineLength = lineEndX - lineStartX
        pointSizeRC = pointSize * 0.5f
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //progress line
        linePaint?.let {
            canvas.drawRoundRect(
                lineStartX,
                centerY - lineHeighC,
                lineEndX,
                centerY + lineHeighC,
                lineHeighC,
                lineHeighC,
                it
            )
        }

        //fixed point
        fixedPointPaint?.let {
            if (pressProgress > 0f) {
                val fixedPointSizeR = fixedPointSize * 0.5f * pressProgress
                val fixedPointSpeed = lineLength / (fixedPointCount - 1)
                for (index in 0 until fixedPointCount) {
                    canvas.drawRoundRect(
                        lineStartX - fixedPointSizeR + fixedPointSpeed * index,
                        centerY - fixedPointSizeR,
                        lineStartX + fixedPointSizeR + fixedPointSpeed * index,
                        centerY + fixedPointSizeR,
                        fixedPointSizeR,
                        fixedPointSizeR,
                        it
                    )
                }
            }
        }

        //point
        pointPaint?.let {
            val pointSizeRD = pointSizeRC - (pointSizeRC * 0.15f) * (1f - pressProgress)
            val progressSpeed = lineLength * progressF
            val ctop = centerY - pointSizeRD
            val cleft = lineStartX - pointSizeRD + progressSpeed
            val cright = lineStartX + pointSizeRD + progressSpeed
            val cbottom = centerY + pointSizeRD
            it?.setShadowLayer(pointSizeRD * 0.05f, 0f, 0f, defaultColor)
            canvas.drawRoundRect(
                cleft,
                ctop,
                cright,
                cbottom,
                pointSizeRD,
                pointSizeRD,
                it
            )
            if (!TextUtils.isEmpty(number) && isShowNumber) {
                val pointCenterX = cleft + (cright - cleft) * 0.5f
                val pointCenterY = (cbottom - ctop) * 0.5f + ctop
                val progressC = (1f / (fixedPointCount - 1)) * 0.5f
                number = "${(1 + (fixedPointCount - 1) * (progressF + progressC)).toInt()}"
                numberPaint?.let { np ->
                    np?.getTextBounds(number, 0, number!!.length, numberRect)
                    val textCenterY = (pointCenterY) + numberRect.height() * 0.5f
                    canvas?.drawText(number!!, pointCenterX, textCenterY, np)
                }
            }
        }
    }

    private fun getColor(startColor: Int, endColor: Int, progress: Float): Int {
        fun ave(src: Int, dst: Int, progress: Float): Int {
            return src + (progress * (dst - src)).roundToInt()
        }

        val a = ave(Color.alpha(startColor), Color.alpha(endColor), progress)
        val r = ave(Color.red(startColor), Color.red(endColor), progress)
        val g = ave(Color.green(startColor), Color.green(endColor), progress)
        val b = ave(Color.blue(startColor), Color.blue(endColor), progress)
        return Color.argb(a, r, g, b)
    }

    private var isPress: Boolean = false
    private var isPointClicked: Boolean = false
    private var isPointMove: Boolean = false
    private var pressProgressF: Float = 0f
    private var pressDownX: Float = 0f
    private var pressDownY: Float = 0f
    private var scrollX: Float = 0f
    private var scrollY: Float = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                scrollX = event?.rawX - pressDownX
                scrollY = event?.rawY - pressDownY
                isPointMove = true
                if (isPointClicked) updateMoveProgress()
            }
            MotionEvent.ACTION_DOWN -> {
                pressDownX = event?.rawX
                pressDownY = event?.rawY
                scrollX = 0f
                scrollY = 0f
                isPointClicked = false
                isPointMove = false
                pressProgressF = progressF
                checkPointClicked(event?.x, event?.y, 100f)
                startPressAnimation(true)
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                isPointClicked = false
                startPressAnimation(false)
                if (isPointMove) startAdsorb(progressF,true)
                parent?.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_CANCEL -> {
                isPointClicked = false
                startPressAnimation(false)
                if (isPointMove) startAdsorb(progressF,true)
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun checkPointClicked(pressX: Float, pressY: Float, offset: Float? = null) {
        val progressSpeed = lineLength * progressF
        val pointRectF = RectF(
            lineStartX - pointSizeRC + progressSpeed - (offset ?: 0f),
            centerY - pointSizeRC - (offset ?: 0f),
            lineStartX + pointSizeRC + progressSpeed + (offset ?: 0f),
            centerY + pointSizeRC + (offset ?: 0f)
        )
        if (pointRectF.contains(pressX, pressY)) {
            isPointClicked = true
        }
    }

    private fun updateMoveProgress() {
        progressF = (pressProgressF + scrollX / width)?.let {
            var lastProgress = it
            if (lastProgress > 1f) lastProgress = 1f
            if (lastProgress < 0) lastProgress = 0f
            lastProgress
        }
        listener?.let {
            it.onProgress(progressF, (progressF * 100).toInt())
        }
        invalidate()
    }

    fun setProgress(progress: Float, isAnimation: Boolean? = false) {
        if (isPointClicked) return
       var targetProgressF  = progress?.let {
            when {
                progress < 0f -> {
                    0f
                }
                progress > 1f -> {
                    1f
                }
                else -> {
                    it
                }
            }
        }

        if (isAnimation == true) {
            startAdsorb(targetProgressF,false)
        } else {
            progressF = targetProgressF
            invalidate()
        }

    }

    private fun startAdsorb(currentProgressF: Float,isCallback: Boolean? = false) {
        var eprogress = 1f / (fixedPointCount - 1f)
        var min: Float? = null
        var minIndex = 0
        for (index in 0 until fixedPointCount) {
            val nprogress = eprogress * index
            val nmin = abs(nprogress - currentProgressF)
            if (min == null || nmin < min) {
                min = nmin
                minIndex = index
            }
        }
        val targetProgress = minIndex * eprogress
        startAdsorbAnimation(targetProgress) {
            if (isCallback == true) {
                listener?.onSelected((targetProgress * 100).toInt(), minIndex)
            }
        }
    }

    private var adsorbValueAnimator: ValueAnimator? = null

    private fun startAdsorbAnimation(targetProgress: Float, oae: (() -> Unit)?) {
        adsorbValueAnimator?.cancel()
        adsorbValueAnimator = ValueAnimator.ofFloat(progressF, targetProgress)
        adsorbValueAnimator?.duration = (150f+(300f*abs(progressF-targetProgress))).toLong()
        adsorbValueAnimator?.addUpdateListener {
            progressF = it.animatedValue as Float
            invalidate()
        }
        adsorbValueAnimator?.addListener(onEnd = {
            oae?.let { it() }
        })
        adsorbValueAnimator?.interpolator = interpolator
        adsorbValueAnimator?.start()
    }

}

interface OnAnProgressViewListener {
    fun onSelected(progress: Int, index: Int)
    fun onProgress(progressF: Float, progress: Int)
}