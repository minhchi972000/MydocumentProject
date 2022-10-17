package wee.digital.sample.ui.fragment.dialog.alert

import android.view.View
import android.widget.TextView
import wee.digital.sample.databinding.AlertBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.main.MainDialog

open class AlertFragment : MainDialog<AlertBinding>() {

    protected val arg get() = dialogVM.alertLiveData.value

    /**
     * [MainDialog] implements
     */
    override fun inflating(): Inflating = AlertBinding::inflate

    override fun onViewCreated() {
    }

    override fun onLiveDataObserve() {
        dialogVM.alertLiveData.observe {
            if (it != null) {
                onBindArg(it)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        arg?.also { onBindDismiss(it) }
        dialogVM.alertLiveData.value = null
    }

    override fun onBackPressed() {
        clickedView = vb.viewClose
        super.onBackPressed()
    }

    /**
     * [AlertFragment] properties
     */
    protected open fun onBindArg(it: AlertArg) {
        //vb.viewClose.isShow(it.dismissWhenTouchOutside)
        vb.viewClose.addClickListener { onBackPressed() }
        if (it.dismissWhenTouchOutside) {
            vb.layoutContent.addClickListener { onBackPressed() }
        }
        vb.imageViewIcon.setImageResource(it.icon)
        vb.textViewTitle.text = it.title
        vb.textViewMessage.setHyperText(it.message)
        vb.viewAccept.setBackgroundResource(it.acceptBackground)
        vb.viewAccept.onBindButton(it.acceptLabel) {
            clickedView = it
            dismiss()
        }
        vb.viewCancel.onBindButton(it.cancelLabel) {
            clickedView = it
            dismiss()
        }
    }

    private fun onBindDismiss(it: AlertArg) {
        when (clickedView) {
            vb.viewAccept -> it.acceptOnClick?.invoke()
            vb.viewCancel -> it.cancelOnClick?.invoke()
            vb.viewClose -> it.onCloseClick?.invoke()
        }
        it.onDismiss?.invoke()
    }

    fun TextView.onBindButton(label: String?, onClick: (View) -> Unit) {
        isGone(label.isNullOrEmpty())
        text = label
        addClickListener { onClick(this) }
    }


}