package wee.digital.widget.custom.base

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import wee.digital.widget.R
import wee.digital.widget.extension.animateColor
import wee.digital.widget.extension.color

class DialogLayout : ConstraintLayout {

    val backgroundColor get() = color(R.color.colorDialogBackground)

    private val statusBarHeight: Int
        get() {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) return resources.getDimensionPixelSize(resourceId)
            return 0
        }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        when (context) {
            is androidx.appcompat.view.ContextThemeWrapper,
            is android.view.ContextThemeWrapper,
            is AppCompatActivity -> {
                setPadding(0, statusBarHeight, 0, 0)
                animateColor(Color.TRANSPARENT, backgroundColor) {
                    setBackgroundColor(it)
                }.also {
                    it.duration = 300
                    it.startDelay = 600
                }.start()
            }
            else -> {
                setBackgroundColor(backgroundColor)
            }
        }
        setOnTouchListener { _, _ ->
            true
        }
    }


    fun animateDismiss(onEnd: () -> Unit) {
        val anim = animateColor(backgroundColor, Color.TRANSPARENT) {
            setBackgroundColor(it)
        }.also {
            it.duration = 400
            it.doOnEnd { onEnd() }
        }
        anim.start()
    }
}
