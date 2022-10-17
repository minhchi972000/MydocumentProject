package wee.digital.widget.custom.base

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.core.widget.NestedScrollView


class AppScrollView : NestedScrollView {

    var scrollable = true

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onInterceptTouchEvent(ev)
    }

}