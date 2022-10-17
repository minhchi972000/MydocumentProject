package wee.digital.widget.extension

import android.view.View
import android.widget.EditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var lastClickViewId: Int = 0

private var lastEventId: Int = -2

private var lastClickTime: Long = System.currentTimeMillis()

abstract class ViewClickListener(
    private val delayedInterval: Long = 300,
    private val eventId: Int = 1
) :
    View.OnClickListener {

    @Volatile
    var onTrigger: Boolean = false

    private val isDelayed: Boolean get() = System.currentTimeMillis() - lastClickTime > delayedInterval

    private val View.isAcceptClick: Boolean get() = id != lastClickViewId

    abstract fun onClicks(v: View)

    final override fun onClick(v: View?) {
        v ?: return
        if (eventId > 0 && eventId == lastEventId) return
        if (onTrigger) return
        if (v.isAcceptClick || isDelayed) {
            onTrigger = true
            lastClickViewId = v.id
            lastClickTime = System.currentTimeMillis()
            lastEventId = eventId
            onClicks(v)
            GlobalScope.launch {
                delay(delayedInterval)
                lastEventId = -2
                onTrigger = false
            }
        }
    }

}

fun View?.addClickListener(
    delayedInterval: Long,
    eventId: Int,
    listener: ((View?) -> Unit)? = null
) {
    this ?: return
    if (listener == null) {
        setOnClickListener(null)
        if (this is EditText) {
            isFocusable = true
            isCursorVisible = true
        }
        return
    }
    setOnClickListener(object : ViewClickListener(delayedInterval, eventId) {
        override fun onClicks(v: View) {
            listener(v)
        }
    })
    if (this is EditText) {
        isFocusable = false
        isCursorVisible = false
    }
}

fun View?.addClickListener(delayedInterval: Long, listener: ((View?) -> Unit)? = null) {
    addClickListener(delayedInterval, 1, listener)
}

fun View?.addClickListener(listener: ((View?) -> Unit)? = null) {
    addClickListener(600, 1, listener)
}

fun addClickListeners(vararg views: View?, block: (View?) -> Unit) {
    val listener = object : ViewClickListener() {
        override fun onClicks(v: View) {
            block(v)
        }
    }
    views.forEach {
        it?.setOnClickListener(listener)
    }
}

fun clearClickListeners(vararg views: View?) {
    views.forEach {
        it?.setOnClickListener(null)
    }
}

abstract class FastClickListener(private val clickCount: Int) : View.OnClickListener {

    private var lastClickTime: Long = 0

    private var currentClickCount: Int = 0

    abstract fun onViewClick(v: View?)

    final override fun onClick(v: View?) {
        if (System.currentTimeMillis() - lastClickTime > 420 || currentClickCount >= clickCount) {
            currentClickCount = 0
        }
        lastClickTime = System.currentTimeMillis()
        currentClickCount++
        if (currentClickCount == clickCount) {
            lastClickTime = 0
            currentClickCount = 0
            onViewClick(v)
        }
    }
}

fun View?.addFastClickListener(clickCount: Int, block: () -> Unit) {
    this?.setOnClickListener(object : FastClickListener(clickCount) {
        override fun onViewClick(v: View?) {
            block()
        }
    })
}

