package wee.digital.widget.util

import android.util.Log

class Logger {

    private val tag: String

    constructor(string: String) {
        this.tag = if (string.length > 23) string.substring(0, 22) else string
    }

    fun d(any: String?) {
        if (enable) Log.d(tag, any ?: "null")
    }

    fun d(throwable: Throwable?) {
        d(throwable?.message)
    }

    fun i(any: String?) {
        if (enable) Log.i(tag, any ?: "null")
    }

    fun i(throwable: Throwable?) {
        i(throwable?.message)
    }

    fun e(any: String?) {
        if (enable) Log.e(tag, any ?: "null")
    }

    fun e(throwable: Throwable?) {
        e(throwable?.message)
    }

    fun w(any: String?) {
        if (enable) Log.w(tag, any ?: "null")
    }

    fun w(throwable: Throwable?) {
        w(throwable?.message)
    }

    fun wtf(any: String?) {
        if (enable) Log.wtf(tag, any ?: "null")
    }

    fun wtf(throwable: Throwable?) {
        wtf(throwable?.message)
    }

    companion object {
        var enable: Boolean = true
    }

}