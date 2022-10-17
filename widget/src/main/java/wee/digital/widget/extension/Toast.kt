package wee.digital.widget.extension

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import wee.digital.widget.R
import wee.digital.widget.app
import wee.digital.widget.databinding.ToastBinding

val TOAST_SUCCESS get() = 0
val TOAST_FAILURE get() = 1
var currentToast: Toast? = null
var toastJob: Job? = null
var networkErrorShown: Long = 0L

fun toastSuccess(message: String?) {
    showToast(TOAST_SUCCESS, message)
}

fun toastError(message: String?) {
    showToast(TOAST_FAILURE, message)
}

fun toastError(t: Throwable?) {
    val s = t?.message
    if (s.isNullOrEmpty()) return
    toastError(s)
}

fun showToast(theme: Int, message: String? = null, duration: Long = 2000) {
    message ?: return
    toastJob?.cancel()
    toastJob = GlobalScope.launch(Dispatchers.Main) {
        val bind = ToastBinding.inflate(LayoutInflater.from(app))
        when (theme) {
            TOAST_SUCCESS -> {
                bind.imageViewIcon.setImageResource(R.drawable.ic_toast_success)
                bind.toastLayout.strokeLineColor = color(R.color.color_success)
            }
            TOAST_FAILURE -> {
                bind.imageViewIcon.setImageResource(R.drawable.ic_toast_failure)
                bind.toastLayout.strokeLineColor = color(R.color.color_error)
            }
        }
        bind.textViewMessage.text = message
        currentToast?.cancel()
        showToast(bind.root)
    }
}

fun showToast(v: View) {
    currentToast = Toast(v.context).also { t ->
        t.duration = Toast.LENGTH_SHORT
        t.setGravity(Gravity.FILL_HORIZONTAL or Gravity.TOP, 0, 0)
        @Suppress("DEPRECATION")
        t.view = v
        t.show()
    }
}
