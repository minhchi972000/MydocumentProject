package wee.digital.widget.extension

import android.os.Build
import android.text.Html
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 123456789012345 -> 1234 4567 8901 2345
 */
fun String?.toCreditNum(): String? {
    return if (this.isNullOrEmpty()) null else replace("\\d{4}".toRegex(), "$0 ")
}

/**
 * 123456789012345 -> •••• •••• •••• 2345
 */
fun String?.toHiddenCreditNum(): String {
    if (this == null || length < 17) return "•••• •••• •••• ••••"
    return "•••• •••• •••• ${substring(lastIndex - 4, lastIndex)}"
}

private val decimalFormat = DecimalFormat("#,###,###,###,###", DecimalFormatSymbols(Locale.US))

const val cashUnit = "đ"

fun Long.moneyFormat(prefix: String = cashUnit): String {
    return this.toString().moneyFormat(prefix)
}

fun BigDecimal.moneyFormat(prefix: String = cashUnit): String {
    return this.toString().moneyFormat(prefix)
}

fun String?.moneyFormat(prefix: String = cashUnit): String {
    this ?: return ""
    return try {
        var originalString = this.replace(".", "")
        if (originalString.contains(".")) {
            originalString = originalString.replace(".".toRegex(), "")
        }
        val value = originalString.toLong()
        "${decimalFormat.format(value)}$prefix".replace(",", ".")
    } catch (nfe: Exception) {
        ""
    }
}

fun String?.moneyValue(): BigDecimal {
    if (this.isNullOrEmpty()) return BigDecimal.ZERO
    return replace(".", "")
        .replace(cashUnit, "")
        .toBigDecimalOrNull() ?: BigDecimal.ZERO
}

fun String?.unHyper(): String? {
    this ?: return null
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(this, 1).toString()
        else -> @Suppress("DEPRECATION")
        Html.fromHtml(this).toString()
    }
}

fun String?.jsonFormat(): String? {
    this ?: return null
    return try {
        val obj = JSONObject(this)
        obj.keys().forEach {
            if (obj.getString(it).length > 256) {
                obj.put(it, obj.getString(it).substring(0, 256) + "...")
            }
        }
        obj.toString(2)
    } catch (ignore: Exception) {
        null
    }
}

fun Long.cashToText(): String {

    val number = this.toString()

    var text = ""
    var startIndex = number.length - 3
    var endIndex = number.length
    var unit = " đồng"

    while (startIndex >= -2) {

        val sCash = number.substring(if (startIndex > -1) startIndex else 0, endIndex)

        text = " ${cashText(sCash)}$unit$text"
        startIndex -= 3
        endIndex -= 3
        unit = when (unit) {
            " nghìn" -> " triệu"
            " triệu" -> " tỷ"
            " đồng" -> " nghìn"
            else -> " nghìn"
        }
    }

    text = text.replace("  ", " ")
        .trim()
        .replace("tỷ triệu nghìn đồng", "tỷ đồng")
        .replace("triệu nghìn đồng", "triệu đồng")

    return text.substring(0, 1).uppercase() + text.substring(1, text.length)
}

fun String?.hideText(replacement: String, visibleCount: Int): String {
    this ?: return ""
    if (length < visibleCount) return ""
    val showText = substring(length - visibleCount)
    val hiddenText = substring(0, length - visibleCount).replace("[^.]".toRegex(), replacement)
    return "$hiddenText $showText"
}

private fun cashText(numText: String): String {

    val n = numText.toLong()
    val n100 = n / 100
    val n10 = n / 10 % 10
    val n1 = n % 10

    if (n100 == 0L && n10 == 0L && n1 == 0L) return ""

    val s100 = if (numText.length < 3) "" else when (n100) {
        0L -> "không trăm"
        1L -> "một trăm"
        2L -> "hai trăm"
        3L -> "ba trăm"
        4L -> "bốn trăm"
        5L -> "năm trăm"
        6L -> "sáu trăm"
        7L -> "bảy trăm"
        8L -> "tám trăm"
        else -> "chín trăm"
    }

    val s10 = if (numText.length < 2) "" else when (n10) {
        0L -> if (n1 == 0L) "" else " lẻ"
        1L -> " mười"
        2L -> " hai mươi"
        3L -> " ba mươi"
        4L -> " bốn mươi"
        5L -> " năm mươi"
        6L -> " sáu mươi"
        7L -> " bảy mươi"
        8L -> " tám mươi"
        else -> " chín mươi"
    }

    val s1 = when (n1) {
        0L -> ""
        1L -> if (n10 < 2) " một" else " mốt"
        2L -> " hai"
        3L -> " ba"
        4L -> " bốn"
        5L -> if (n10 == 0L) " năm" else " lăm"
        6L -> " sáu"
        7L -> " bảy"
        8L -> " tám"
        else -> " chín"
    }
    return "$s100$s10$s1"

}