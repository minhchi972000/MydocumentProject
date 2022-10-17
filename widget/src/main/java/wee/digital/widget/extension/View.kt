package wee.digital.widget.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

fun View.requireActivity(): Activity? {
    val lifecycleOwner = this.findViewTreeLifecycleOwner()
    if (lifecycleOwner is Activity) return lifecycleOwner
    if (lifecycleOwner is Fragment) return lifecycleOwner.requireActivity()
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun View.dpToPx(value: Float): Float {
    val scale = context.resources.displayMetrics.density
    return (value * scale + 0.5f)
}

fun show(vararg views: View) {
    for (v in views) v.show()
}

fun hide(vararg views: View) {
    for (v in views) v.hide()
}

fun gone(vararg views: View) {
    for (v in views) v.gone()
}

val View.activity: Activity? get() = context as? Activity

fun View.color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(context, res)
}

/**
 * [View] visible state
 */
val View?.lifecycleScope: LifecycleCoroutineScope?
    get() = this?.findViewTreeLifecycleOwner()?.lifecycleScope

fun View?.launch(block: () -> Unit) {
    lifecycleScope?.launch(Dispatchers.Main) {
        block()
    }
}

fun View?.launch(delayInterval: Long, block: () -> Unit) {
    lifecycleScope?.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) { delay(delayInterval) }
        block()
    }
}

fun View.show() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.isShow(show: Boolean?) {
    visibility = if (show == true) View.VISIBLE
    else View.INVISIBLE
}

fun View.hide() {
    if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.isHide(hide: Boolean?) {
    visibility = if (hide == true) View.INVISIBLE
    else View.VISIBLE
}

fun View.gone() {
    if (visibility != View.GONE) visibility = View.GONE
}

fun View.isGone(gone: Boolean?) {
    visibility = if (gone == true) View.GONE
    else View.VISIBLE
}

fun View?.post(delayed: Long, runnable: Runnable) {
    this?.postDelayed(runnable, delayed)
}

/**
 * @param animationStyle animationStyle
 * <style name="PopupStyle">
 *      <item name="android:windowEnterAnimation">@anim/anim1</item>
 * </style>
 */
fun View.showPopup(
    @LayoutRes layoutRes: Int,
    @StyleRes animationStyle: Int,
    block: (View, PopupWindow) -> Unit
) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val v = inflater.inflate(layoutRes, null)
    val popup = PopupWindow(
        v,
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )
    popup.animationStyle = animationStyle
    popup.showAsDropDown(this)
    block(v, popup)
}

val View?.backgroundColor: Int
    get() {
        this ?: return Color.WHITE
        this.background ?: return Color.WHITE
        var color = Color.TRANSPARENT
        val background: Drawable = this.background
        if (background is ColorDrawable) color = background.color
        return color
    }

fun View.backgroundTint(@ColorInt color: Int) {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            background?.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_OVER)
        } else {
            @Suppress("DEPRECATION")
            background?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}

fun View.backgroundTintRes(@ColorRes colorRes: Int) {
    backgroundTint(color(colorRes))
}

fun View.getBitmap(): Bitmap {
    val lp = this.layoutParams
    val b = Bitmap.createBitmap(lp.width, lp.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    this.layout(this.left, this.top, this.right, this.bottom)
    this.draw(c)
    return b
}

fun View.getBitmapColor(defaultColor: Int = -1): Bitmap? {
    var bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    if (defaultColor > 0) canvas.drawColor(defaultColor)
    this.draw(canvas)
    return bitmap
}

fun View?.getLocation(): Point {
    return try {
        val ints = IntArray(2)
        this?.getLocationOnScreen(ints)
        Point(ints.first(), ints.last())
    } catch (e: Exception) {
        Point(0, 0)
    }
}

fun View?.hideKeyboard() {
    this?.post {
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun View?.showKeyboard() {
    this?.post {
        requestFocus()
        val imm: InputMethodManager? =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun clearBackground(vararg views: View) {
    views.forEach { it.background = null }
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun ViewGroup.isEnableChildren(enabled: Boolean) {
    val childCount = this.childCount
    for (i in 0 until childCount) {
        val view = this.getChildAt(i)
        view.isEnabled = enabled
        if (view is ViewGroup) {
            view.isEnableChildren(enabled)
        }
    }
}

fun ViewGroup.enableChildren() {
    isEnableChildren(true)
}

fun ViewGroup.disableChildren() {
    isEnableChildren(false)
}

@ColorInt
fun Int.darker(factor: Float): Int {
    val a = Color.alpha(this)
    val r = (Color.red(this) * factor).toDouble().roundToInt()
    val g = (Color.green(this) * factor).toDouble().roundToInt()
    val b = (Color.blue(this) * factor).toDouble().roundToInt()
    return Color.argb(
        a,
        r.coerceAtMost(255),
        g.coerceAtMost(255),
        b.coerceAtMost(255)
    )
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, marginTop, 0, 0)
    this.layoutParams = menuLayoutParams
}

fun View.setMarginBottom(marginBottom: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, 0, 0, marginBottom)
    this.layoutParams = menuLayoutParams
}