package wee.digital.widget.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.viewbinding.ViewBinding
import wee.digital.widget.custom.base.AppCustomView
import wee.digital.widget.databinding.PinBinding
import wee.digital.widget.extension.DIGIT_FILTER
import wee.digital.widget.extension.SimpleTextWatcher
import wee.digital.widget.extension.addFilter

class PinView(context: Context, attrs: AttributeSet? = null) :
    AppCustomView<PinBinding>(context, attrs) {

    companion object {
        const val MAX_LENGTH = 4
    }

    var onFilled: ((String) -> Unit)? = null

    var onChange: ((String) -> Unit)? = null

    var filledValue: String? = null

    var error: String?
        get() = vb.pinTextViewError.text?.toString()
        set(value) {
            vb.pinTextViewError.text = value
        }

    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return PinBinding::inflate
    }

    override fun onInitialize(context: Context, types: TypedArray) {
        vb.pinEditText.addFilter(DIGIT_FILTER)
        vb.pinEditText.addFilter(InputFilter.LengthFilter(MAX_LENGTH))
        vb.pinEditText.inputType = EditorInfo.TYPE_CLASS_NUMBER
        vb.pinEditText.requestFocus()
        vb.pinEditText.transformationMethod = TransformMethod()
        vb.pinEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: String) {
                if (!filledValue.isNullOrEmpty()) {
                    vb.pinEditText.setTextSilently(filledValue)
                    return
                }
                onChange?.invoke(s)
                if (s.length == MAX_LENGTH && filledValue.isNullOrEmpty()) {
                    filledValue = s
                    onFilled?.invoke(s)
                }
            }
        })
        onChange = { if (it.isNotEmpty()) error = null }
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return vb.pinEditText.requestFocus(direction, previouslyFocusedRect)
    }

    fun clear() {
        filledValue = null
        vb.pinEditText.text = null
    }

    private class TransformMethod : PasswordTransformationMethod() {

        companion object {

            private const val MASK2 = '●'
        }

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(source)
        }

        private inner class PasswordCharSequence(private val source: CharSequence) : CharSequence {

            override val length: Int get() = source.length

            override fun get(index: Int): Char {
                return when {
                    length < 0 -> return source[index]
                    index < length - 0 -> MASK2
                    else -> source[index]
                }
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return source.subSequence(startIndex, endIndex)
            }

        }

    }


}