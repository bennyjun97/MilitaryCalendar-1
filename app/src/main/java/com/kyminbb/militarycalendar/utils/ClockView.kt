package com.kyminbb.militarycalendar.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View

class ClockView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var cX = 0f
    private var cY = 0f
    private var mPadding = 16f

    private var mRadius = 0f
    private var mHandLength = 0f

    private var hour = 2
    private var minute = 30

    private val edgePaint = Paint(ANTI_ALIAS_FLAG).apply {
        context?.let {
            color = Color.DKGRAY
        }
        style = Paint.Style.STROKE
        strokeWidth = 28f
    }
    private val bodyPaint = Paint(ANTI_ALIAS_FLAG).apply {
        context?.let {
            color = Color.WHITE
        }
        style = Paint.Style.FILL
    }
    private val centerPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val handPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cX = w / 2f
        cY = h / 2f
        mRadius = Math.min(cX, cY) - mPadding
        mHandLength = mRadius * 3 / 4
        edgePaint.strokeWidth = mRadius * 0.1f
        handPaint.strokeWidth = mRadius * 0.08f
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(cX, cY, mRadius, bodyPaint)
        canvas?.drawCircle(cX, cY, mRadius, edgePaint)
        canvas?.drawCircle(cX, cY, edgePaint.strokeWidth, centerPaint)

        val moment = (hour + (minute / 60).toDouble()) * 5
        drawHand(canvas, moment, true)
        drawHand(canvas, minute.toDouble(), false)
    }

    fun onTimeChanged(h: Long, m: Long) {
        hour = h.toInt() % 12
        minute = m.toInt()

        invalidate()
    }

    private fun drawHand(canvas: Canvas?, moment: Double, isHour: Boolean) {
        val angle = Math.PI * moment / 30 - Math.PI / 2
        var handRadius = mHandLength
        // make hour hand shorter than minute hand
        if (isHour) {
            handRadius = mHandLength * 2 / 3
        }
        // get coordinates of hand's endpoint
        val endX = (cX + Math.cos(angle) * handRadius).toFloat()
        val endY = (cY + Math.sin(angle) * handRadius).toFloat()
        canvas?.drawLine(cX, cY, endX, endY, handPaint)
    }
}