package com.arjunsahlot.blindvision

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

class TouchListenableButton : AppCompatButton {

    var onTouchUp: (() -> Unit)? = null
    var onTouchDown: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        setupTouchListener()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupTouchListener()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupTouchListener()
    }

    private fun setupTouchListener() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onTouchDown?.invoke()
                MotionEvent.ACTION_UP -> {
                    onTouchUp?.invoke()
                    performClick()
                }
            }
            true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
