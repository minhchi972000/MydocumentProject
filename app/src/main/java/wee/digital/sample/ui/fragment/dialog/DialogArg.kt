package wee.digital.sample.ui.fragment.dialog


abstract class DialogArg {

    var dismissWhenTouchOutside: Boolean = true

    var onCloseClick: (() -> Unit)? = null

    var onDismiss: (() -> Unit)? = null

    fun onCloseClick(block: () -> Unit) {
        onCloseClick = block
    }

    fun onDismiss(block: () -> Unit) {
        onDismiss = block
    }

    var onBackClick: (() -> Unit)?
        get() = onCloseClick
        set(value) {
            onCloseClick = value
        }
}