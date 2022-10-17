package wee.digital.widget.extension

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.graphics.*
import android.os.Build
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView

fun ImageView.tint(@ColorInt color: Int? = null) {
    post {
        if (color == null) {
            clearColorFilter()
            colorFilter = null
            return@post
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

fun ImageView.tintRes(@ColorRes res: Int) {
    tint(ContextCompat.getColor(context, res))
}

fun ImageView.grayScale() {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0.0f)
    colorFilter = ColorMatrixColorFilter(colorMatrix)
}

val NestedScrollView?.hasInvisibleScrollContent: Boolean
    @SuppressLint("RestrictedApi")
    get() {
        this ?: return false
        return this.scrollY < (this.computeVerticalScrollRange() - this.height)
    }

fun NestedScrollView.smoothScrollToTop() {
    post {
        fling(0)
        smoothScrollTo(0, 0)
    }
}

fun NestedScrollView.smoothScrollTo(view: View) {
    post {
        val top = view.top
        val bot = view.bottom
        val height = this.height
        this.smoothScrollTo(0, (top + bot - height) / 2)
    }
}

fun NestedScrollView.addOnScrollListener(block: (NestedScrollView) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.setOnScrollChangeListener { _, _, _, _, _ ->
            block(this)
        }
    } else {
        this.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            block(this)
        })
    }
}

fun HorizontalScrollView.smoothScrollTo(view: View) {
    post {
        val left = view.left
        val right = view.right
        val width = this.width
        this.smoothScrollTo((left + right - width) / 2, 0)
    }
}

fun RadioGroup.checkedButton(): View? {
    for (i in 0 until this.childCount) {
        val v = this.getChildAt(i)
        if (v is RadioButton && v.isChecked)
            return v
    }
    return null
}

fun RadioGroup.addOnCheckedChangeListener(block: (RadioButton) -> Unit) {
    setOnCheckedChangeListener { _, checkedId ->
        val button = (context as Activity).findViewById<RadioButton>(checkedId)
        block(button)
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupWebView() {
    settings.builtInZoomControls = true
    settings.displayZoomControls = true
    settings.javaScriptEnabled = true
    settings.defaultTextEncodingName = "utf-8"
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupWebViewPDF() {
    settings.builtInZoomControls = true
    settings.displayZoomControls = true
    settings.javaScriptEnabled = true
    overScrollMode = WebView.OVER_SCROLL_NEVER
}

fun WebView.setChromeClient(progressBar: ProgressBar) {

    webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            try {
                if (progress < 100 && progressBar.visibility == View.GONE) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = progress
                } else {
                    progressBar.visibility = View.GONE
                }
            } catch (ex: IllegalStateException) {
            }
        }
    }
}

fun WebView.setClient(progressBar: ProgressBar) {

    webViewClient = object : WebViewClient() {
        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            view?.loadUrl(url)
            return super.shouldOverrideUrlLoading(view, url)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            try {
                progressBar.visibility = View.VISIBLE
            } catch (ex: IllegalStateException) {
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            try {
                progressBar.visibility = View.INVISIBLE
            } catch (ex: IllegalStateException) {
            }
            super.onPageFinished(view, url)
        }
    }
}
