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

    private var hour = 3
    private var minute = 30
    private var second = 45

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
    private val centerPaintRed = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val handPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 16f
    }
    private val secondPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 13f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cX = w / 2f
        cY = h / 2f
        mRadius = Math.min(cX, cY) - mPadding * 2f
        mHandLength = mRadius * 4 / 5
        edgePaint.strokeWidth = mRadius * 0.1f
        handPaint.strokeWidth = mRadius * 0.08f
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(cX, cY, mRadius, bodyPaint)
        canvas?.drawCircle(cX, cY, mRadius, edgePaint)
        canvas?.drawCircle(cX, cY, 35f, centerPaint)

        val moment = (hour.toDouble() + (minute.toDouble() / 60)) * 5
        drawHand(canvas, moment, 1)
        drawHand(canvas, minute.toDouble(), 2)

        drawHand(canvas, second.toDouble(), 3)
        canvas?.drawCircle(cX, cY, 15f, centerPaintRed)
    }

    fun onTimeChanged(h: Long, m: Long, s: Long) {
        hour = h.toInt() % 12
        minute = m.toInt()
        second = s.toInt()

        invalidate()
    }

    private fun drawHand(canvas: Canvas?, moment: Double, type: Int) {
        val angle = Math.PI * moment / 30 - Math.PI / 2
        var handRadius = mHandLength
        // make hour hand shorter than minute hand
        if (type == 1) {
            handRadius = mHandLength * 2 / 3
        }
        // get coordinates of hand's endpoint
        val endX = (cX + Math.cos(angle) * handRadius).toFloat()
        val endY = (cY + Math.sin(angle) * handRadius).toFloat()

        //when its seconds
        if (type == 3) {
            canvas?.drawLine(cX, cY, endX, endY, secondPaint)
            val endX = (cX - Math.cos(angle) * handRadius / 5).toFloat()
            val endY = (cY - Math.sin(angle) * handRadius / 5).toFloat()
            canvas?.drawLine(cX, cY, endX, endY, secondPaint)
        }
        else {
            canvas?.drawLine(cX, cY, endX, endY, handPaint)
        }
    }
}