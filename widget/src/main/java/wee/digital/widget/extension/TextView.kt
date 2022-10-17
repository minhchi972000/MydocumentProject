package wee.digital.widget.extension

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.InputType.TYPE_NULL
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import wee.digital.widget.R
import wee.digital.widget.app


fun charsFilter(chars: CharArray): InputFilter {
    return InputFilter { source, start, end, _, _, _ ->
        when {
            end > start -> for (index in start until end) {
                if (!String(chars).contains(source[index].toString())) {
                    return@InputFilter ""
                }
            }
        }
        return@InputFilter null
    }
}

fun EditText?.addOnClickListener(listener: View.OnClickListener) {
    this ?: return
    isFocusable = false
    isCursorVisible = false
    keyListener = null
    inputType = EditorInfo.IME_ACTION_NONE
    setOnClickListener { listener.onClick(this) }
}

val EditText.hasError: Boolean get() = !error.isNullOrEmpty()

fun EditText?.setReadOnly() {
    this ?: return
    isClickable = true
    isFocusable = false
    isCursorVisible = false
    keyListener = null
    inputType = TYPE_NULL
}

fun EditText?.showKeyboard() {
    this?.post {
        requestFocus()
        val imm: InputMethodManager? =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun EditText?.hideKeyboard() {
    this?.post {
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun EditText.addFilter(filter: InputFilter) {
    val newFilter = mutableListOf<InputFilter>()
    newFilter.add(filter)
    if (!this.filters.isNullOrEmpty()) {
        newFilter.addAll(this.filters)
    }
    this.filters = newFilter.toTypedArray()
}

fun EditText.addCharsFilter(chars: CharArray) {
    addFilter(charsFilter(chars))
}

fun EditText.disableFocus() {
    hideKeyboard()
    clearFocus()
    isFocusable = false
    isCursorVisible = false
}

fun EditText.enableFocus() {
    isFocusable = true
    isCursorVisible = true
    isEnabled = true
    isFocusableInTouchMode = true
}

val TextView.trimText: String?
    get() {
        val s = this.text?.toString().trimText
        text = s
        if (this is EditText && this.hasFocus()) {
            setSelection(s?.length ?: 0)
        }
        return s
    }


val TextView.trimStart: String?
    get() {
        val s = this.text?.toString()?.trimStart()
        text = s
        if (this is EditText && this.hasFocus()) {
            setSelection(s?.length ?: 0)
        }
        return s
    }

fun EditText.addEditorActionListener(actionId: Int, block: (String?) -> Unit) {
    imeOptions = actionId
    setImeActionLabel(null, actionId)
    setOnEditorActionListener(object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (imeOptions == actionId) {
                this@addEditorActionListener.post {
                    isSelected = false
                    block(text.toString())
                    hideKeyboard()
                    clearFocus()
                }
                return true
            }
            return false
        }
    })
}

fun EditText.addActionNextListener(block: (String?) -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_NEXT
    isSingleLine = true
    setImeActionLabel("Next", EditorInfo.IME_ACTION_NEXT)
    setOnEditorActionListener(object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (imeOptions == actionId) {
                this@addActionNextListener.post {
                    block(text.toString())
                }
                return true
            }
            return false
        }
    })
}

fun EditText.addActionDoneListener(block: (String?) -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    isSingleLine = true
    setImeActionLabel("Next", EditorInfo.IME_ACTION_DONE)
    setOnEditorActionListener(object : TextView.OnEditorActionListener {
        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (imeOptions == actionId) {
                this@addActionDoneListener.post {
                    isSelected = false
                    block(text.toString())
                }
                return true
            }
            return false
        }
    })
}

fun EditText.select() {
    this.setSelection(text?.toString()?.length ?: 0)
}

fun TextView.textColorRes(@ColorRes color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}

fun TextView.setHyperText(@StringRes res: Int, vararg args: Any?) {
    setHyperText(string(res), * args)
}

fun TextView.setHyperText(s: String?, vararg args: Any?) {
    post {
        text = try {
            when {
                s.isNullOrEmpty() -> null
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(
                    s.format(*args),
                    Html.FROM_HTML_MODE_LEGACY
                )
                else -> {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(s.format(*args))
                }
            }
        } catch (e: Throwable) {
            s
        }
    }
}

fun TextView.gradientHorizontal(@ColorRes colorStart: Int, @ColorRes colorEnd: Int = colorStart) {
    paint.shader = LinearGradient(
        0f, 0f, this.width.toFloat(), 0f,
        ContextCompat.getColor(context, colorStart),
        ContextCompat.getColor(context, colorEnd),
        Shader.TileMode.CLAMP
    )
}

fun TextView.gradientVertical(@ColorRes colorStart: Int, @ColorRes colorEnd: Int = colorStart) {
    paint.shader = LinearGradient(
        0f, 0f, 0f, this.height.toFloat(),
        ContextCompat.getColor(context, colorStart),
        ContextCompat.getColor(context, colorEnd),
        Shader.TileMode.CLAMP
    )
}

fun TextView.bold() {
    setTypeface(this.typeface, Typeface.BOLD)
}

fun TextView.regular() {
    typeface = Typeface.create(this.typeface, Typeface.NORMAL)
}

fun TextView.drawableEnd(@DrawableRes res: Int) {
    val drawable: Drawable? = try {
        ContextCompat.getDrawable(context, res)
    } catch (e: Exception) {
        null
    }
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.setSpanClick(fullText: String, spannedText: String, block: () -> Unit) {
    val click = object : ClickableSpan() {
        override fun onClick(widget: View) {
            block()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
        }
    }
    val startIndex = fullText.indexOf(spannedText)
    val endIndex = startIndex + spannedText.length
    val spanStr = SpannableString(fullText)
    spanStr.setSpan(click, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spanStr.setSpan(
        ForegroundColorSpan(color(R.color.color_primary)),
        startIndex,
        endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spanStr.setSpan(
        StyleSpan(Typeface.BOLD),
        startIndex,
        endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    text = spanStr
    linksClickable = true
    isClickable = true
    movementMethod = LinkMovementMethod.getInstance()
}

fun ClipData?.initClipData(item: ClipData.Item): ClipData {
    var clipData = this
    val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (clipData == null) {
        clipData = ClipData(
            ClipDescription(
                "description",
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            ), item
        )
        clipboard.setPrimaryClip(clipData)
    }
    clipData.addItem(item)
    return clipData
}

fun ClipData?.initClipData(label: String, text: String) {
    var clip: ClipData? = this
    val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    if (this == null) {
        clip = ClipData.newPlainText(label, text)
    }
    clipboard!!.setPrimaryClip(clip!!)
}





