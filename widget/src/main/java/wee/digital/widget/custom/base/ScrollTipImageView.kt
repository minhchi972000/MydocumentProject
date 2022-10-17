package wee.digital.widget.custom.base

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.NestedScrollView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wee.digital.widget.extension.addOnScrollListener
import wee.digital.widget.extension.hideKeyboard
import wee.digital.widget.extension.isShow
import wee.digital.widget.extension.lifecycleScope

class ScrollTipImageView : AppCompatImageView {

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        when (context) {
            is AppCompatActivity -> {
                visibility = View.INVISIBLE
            }
        }
    }

    fun sync(scrollView: NestedScrollView) {
        isClickable = false
        lifecycleScope?.launch {
            delay(300)
            isShow(scrollView.hasInvisibleScrollContent)
            scrollView.addOnScrollListener {
                it.hideKeyboard()
                isShow(scrollView.hasInvisibleScrollContent)
            }
        }
    }

    private val NestedScrollView.hasInvisibleScrollContent: Boolean
        @SuppressLint("RestrictedApi")
        get() {
            return this.scrollY < (this.computeVerticalScrollRange() - this.height)
        }
}
