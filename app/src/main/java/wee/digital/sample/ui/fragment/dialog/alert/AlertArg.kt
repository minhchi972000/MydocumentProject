package wee.digital.sample.ui.fragment.dialog.alert

import wee.digital.sample.R
import wee.digital.sample.ui.fragment.dialog.DialogArg
import wee.digital.widget.extension.string

class AlertArg : DialogArg() {
    var icon: Int = R.drawable.ic_check
    var title: String? = "Hello"
    var message: String? = null
    var acceptBackground: Int = R.drawable.btn_primary_bg
    var acceptLabel: String? = "Đóng"
    var cancelLabel: String? = null
    var acceptOnClick: (() -> Unit)? = null
    var cancelOnClick: (() -> Unit)? = null
    var feature: String? = null

    fun acceptOnClick(block: () -> Unit) {
        acceptOnClick = block
    }

    fun cancelOnClick(block: () -> Unit) {
        cancelOnClick = block
    }

}