package wee.digital.sample.ui.fragment.dialog.progress

import wee.digital.sample.databinding.ProgressBinding
import wee.digital.sample.ui.base.BaseDialog
import wee.digital.sample.ui.base.Inflating

class ProgressDialog : BaseDialog<ProgressBinding>() {

    override fun inflating(): Inflating = ProgressBinding::inflate

    override fun onViewCreated() {
    }

    override fun onLiveDataObserve() {
    }

}