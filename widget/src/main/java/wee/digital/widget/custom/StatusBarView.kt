package wee.digital.widget.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import wee.digital.widget.custom.base.AppExpandableLayout

class StatusBarView : AppExpandableLayout {

    companion object {
        var savedStatusBarHeight: Int = 0
    }

    private val statusBarHeight: Int
        get() {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) return resources.getDimensionPixelSize(resourceId)
            return 0
        }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        duration = 400
        when (context) {
            is androidx.appcompat.view.ContextThemeWrapper,
            is android.view.ContextThemeWrapper,
            is AppCompatActivity -> {
                setBackgroundResource(0)
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (savedStatusBarHeight > 0 && visibility == View.VISIBLE) {
            val lp = this.layoutParams
            lp.height = statusBarHeight
            this.layoutParams = lp
        }
    }

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        updateStatusBar()
        super.setExpanded(expand, animate)
    }

    private fun updateStatusBar() {
        if (savedStatusBarHeight == 0) {
            savedStatusBarHeight = statusBarHeight
        }
        val lp = this.layoutParams
        if (lp.height != savedStatusBarHeight) {
            lp.height = savedStatusBarHeight
            this.layoutParams = lp
        }
    }

    fun observer(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(300)
            updateStatusBar()
        }
    }

}