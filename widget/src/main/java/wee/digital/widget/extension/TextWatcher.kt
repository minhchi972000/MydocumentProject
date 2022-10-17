package wee.digital.widget.extension

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

abstract class SimpleTextWatcher : TextWatcher {

    private var saveText: String? = null

    private var ignore: Boolean = false

    open fun EditText.setTextSilently(s: String?) {
        ignore = true
        setText(s)
        handleSelection()
        ignore = false
    }

    open fun EditText.handleSelection() {
        setSelection(text.length)
    }

    final override fun afterTextChanged(s: Editable?) {
        if (saveText == s.toString()) return
        if (ignore) return
        onTextChanged(s.toString())
    }

    final override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        saveText = s.toString()
    }

    final override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    abstract fun onTextChanged(s: String)
}

fun String.getCashDecimal(prefix: String = ""): BigDecimal {
    return try {
        if (this.isNullOrEmpty()) return BigDecimal.ZERO
        val text = this
            .replace(",", "")
            .replace(" ", "")
            .replace(prefix, "")
        text.toBigDecimal()
    } catch (ignore: Exception) {
        BigDecimal.ZERO
    }
}

fun BigDecimal?.getCashString(prefix: String = ""): String {
    this ?: return " $prefix"
    return "${integerCashFmt.format(this)} $prefix"
}

/**
 *
 */
val integerCashFmt = DecimalFormat("#,###,###,###", DecimalFormatSymbols(Locale.US))

fun String?.integerCash(): String {
    this ?: return ""
    return try {
        var originalString = this.replace(".", "")
        if (originalString.contains(",")) {
            originalString = originalString.replace(",".toRegex(), "")
        }
        val value = originalString.toLong()
        integerCashFmt.format(value)
    } catch (ignore: Exception) {
        ""
    }
}

fun EditText.addIntegerCashWatcher() {
    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or
            InputType.TYPE_NUMBER_FLAG_SIGNED
    addTextChangedListener(object : SimpleTextWatcher() {
        override fun onTextChanged(s: String) {
            setTextSilently(text.toString().integerCash())
        }
    })
}

/**
 *
 */
fun EditText.addDateWatcher(prefix: Char = '/') {
    inputType = InputType.TYPE_CLASS_NUMBER
    filters = arrayOf(InputFilter.LengthFilter(10))
    addTextChangedListener(object : SimpleTextWatcher() {

        private val sb: StringBuilder = StringBuilder("")

        override fun onTextChanged(s: String) {
            sb.clear()
            sb.append(if (s.length > 10) s.subSequence(0, 10) else s)
            if (sb.lastIndex == 2 && sb[2] != prefix) {
                sb.insert(2, prefix)
                setTextSilently(sb.toString())
            } else if (sb.lastIndex == 5 && sb[5] != prefix) {
                sb.insert(5, prefix)
                setTextSilently(sb.toString())
            }
        }
    })
}

val EditText.amount: BigDecimal
    get() {
        return try {
            val s = this.text?.toString()
            if (s.isNullOrEmpty()) return BigDecimal.ZERO
            val text = s.toString().replace(",", "")
            text.toBigDecimal()
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

fun TextView.onTextChanged(block: (String) -> Unit) {
    addTextChangedListener(object : SimpleTextWatcher() {
        override fun onTextChanged(s: String) {
            block(s)
        }
    })
}