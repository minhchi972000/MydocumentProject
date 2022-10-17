package wee.digital.widget.extension

import android.util.Base64
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import wee.digital.widget.app
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

val String?.trimText: String?
    get() {
        var s = this ?: return null
        if (s.isNullOrEmpty()) return null
        s = s.replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
            .trimStart()
            .trimEnd()
            .trimIndent()
        return s
    }

val String?.trimEndText: String?
    get() {
        var s = this ?: return null
        if (s.isNullOrEmpty()) return null
        s = s.replace("\n", " ")
            .replace("\\s+".toRegex(), " ")
            .trimStart()
            .trimIndent()
        return s
    }

fun String?.notNullOrEmpty(): Boolean {
    return !this.isNullOrEmpty()
}

fun String.toBase64(flags: Int = Base64.DEFAULT): String {
    return Base64.encodeToString(this.toByteArray(), flags)
}

fun String.fromBase64(flags: Int = Base64.DEFAULT): String {
    return String(Base64.decode(this, flags))
}

fun String.color(hexString: String): String {
    return "<font color=$hexString>$this</font>"
}

fun String.color(@ColorInt color: Int): String {
    val hexString = "#${Integer.toHexString(color and 0x00ffffff)}"
    return this.color(hexString)
}

fun String.colorRes(@ColorRes res: Int): String {
    return this.color(ContextCompat.getColor(app, res))
}

fun String.bold(): String {
    return "<b>$this</b>"
}

fun String?.normalizer(): String? {
    if (this.isNullOrEmpty()) return null
    return try {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        pattern.matcher(temp)
            .replaceAll("")
            .lowercase()
            .replace("đ", "d", true)

    } catch (e: IllegalStateException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun String?.capitalizeWords(): String {
    this ?: return ""
    return split(" ").joinToString(" ") {
        it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }
}






