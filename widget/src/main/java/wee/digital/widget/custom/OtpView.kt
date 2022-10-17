package wee.digital.widget.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import wee.digital.widget.R
import wee.digital.widget.custom.base.AppCustomView
import wee.digital.widget.databinding.OtpBinding
import wee.digital.widget.extension.*

class OtpView(context: Context, attrs: AttributeSet? = null) :
    AppCustomView<OtpBinding>(context, attrs) {

    private lateinit var textViewList: List<TextView>

    private lateinit var containerList: List<View>

    private var index: Int = 0

    var onChange: ((String) -> Unit)? = null

    var onFilled: ((String) -> Unit)? = null

    private var filledValue: String = ""

    var error: String?
        get() = vb.otpTextViewError.text?.toString()
        set(value) {
            post { vb.otpTextViewError.setHyperText(value) }
        }

    var hint: String?
        get() = vb.otpTextViewHint.text?.toString()
        set(value) {
            post { vb.otpTextViewHint.setHyperText(value) }
        }

    private val editText: EditText get() = vb.otpEditText

    val isNotEmpty get() = !isEmpty

    val isEmpty get() = editText.text.isNullOrEmpty()

    var text: String
        get() = editText.text.toString()
        set(value) {
            if (value.length != 4) return
            filledValue = value
            editText.setText(value)
            index = 0
            value.forEach {
                val textView = textViewList[index++]
                textView.text = it.toString()
                val anim = appendTextAnim(textView)
                textView.startAnimation(anim)
            }
            updateCursorUi()
            onFilled?.invoke(value)
        }

    val isFilled get() = filledValue.length == MAX_LENGTH

    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return OtpBinding::inflate
    }

    override fun onInitialize(context: Context, types: TypedArray) {
        textViewList = listOf(
            vb.otpTextView0,
            vb.otpTextView1,
            vb.otpTextView2,
            vb.otpTextView3
        )
        containerList = listOf(
            vb.otpView0,
            vb.otpView1,
            vb.otpView2,
            vb.otpView3
        )
        editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
        editText.addFilter(DIGIT_FILTER)
        editText.addFilter(InputFilter.LengthFilter(MAX_LENGTH))
        editText.addActionDoneListener {
            if (filledValue.length < MAX_LENGTH) requestFocus()
        }
        editText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: String) {
                if (!filledValue.isNullOrEmpty()) {
                    editText.setTextSilently(filledValue)
                    return
                }
                onChange?.invoke(s)
                when (s.length) {
                    in (index + 1)..MAX_LENGTH -> append(s.last().toString())
                    else -> delete()
                }
            }
        })
        editText.setOnFocusChangeListener { _, hasFocus ->
            index = if (hasFocus) editText.selectionEnd else -1
            updateCursorUi()
        }
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        val focus = editText.requestFocus(direction, previouslyFocusedRect)
        editText.showKeyboard()
        return focus
    }

    private fun append(s: String) {
        if (index >= textViewList.size) return
        val textView = textViewList[index++]
        textView.text = s
        textView.clearAnimation()
        val anim = appendTextAnim(textView)
        if (index >= textViewList.size) {
            filledValue = text
            anim.setAnimationListener(object : SimpleAnimationListener {
                override fun onAnimationEnd(animation: Animation) {
                    if (isFilled) {
                        onFilled?.invoke(filledValue)
                    }
                }
            })
        }
        textView.startAnimation(anim)
        updateCursorUi()
    }

    private fun delete() {
        if (index <= 0) return
        textViewList[--index].text = ""
        updateCursorUi()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            vb.otpEditText.isFocusable = true
            vb.otpEditText.isCursorVisible = true
            vb.otpEditText.isEnabled = true
            vb.otpEditText.isFocusableInTouchMode = true
            vb.otpEditText.inputType = InputType.TYPE_CLASS_NUMBER
            vb.otpEditText.requestFocus()
        } else {
            vb.otpEditText.isFocusable = false
            vb.otpEditText.clearFocus()
        }
        updateCursorUi()
    }

    fun clear() {
        index = 0
        filledValue = ""
        editText.text = null
        textViewList.forEach {
            it.clearAnimation()
            it.text = null
        }
        updateCursorUi()
    }

    fun resume() {
        isEnabled = true
        error = null
        hint = null
        requestFocus()
    }

    private fun updateCursorUi() {
        for (i in 0 until MAX_LENGTH) {
            val res = when {
                !isEnabled -> R.drawable.drw_otp_error
                i < index && editText.hasFocus() -> R.drawable.drw_otp_active_bg
                else -> R.drawable.drw_otp_inactive_bg
            }
            containerList[i].setBackgroundResource(res)
        }
    }

    private fun appendTextAnim(v: View): AnimationSet {
        val translateIn: Animation = TranslateAnimation(
            0f, 0f,
            v.height.toFloat(), 0f
        ).also {
            it.interpolator = OvershootInterpolator()
            it.duration = 300
        }
        val fadeIn: Animation = AlphaAnimation(0f, 1f).also {
            it.duration = 300
        }
        return AnimationSet(false).also {
            it.addAnimation(fadeIn)
            it.addAnimation(translateIn)
            it.reset()
            it.startTime = 0
        }
    }

    companion object {
        const val MAX_LENGTH = 4
    }

}