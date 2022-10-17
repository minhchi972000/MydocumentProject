package wee.digital.sample.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import wee.digital.sample.ui.base.FragmentView
import wee.digital.sample.ui.fragment.dialog.DialogVM
import wee.digital.sample.ui.fragment.dialog.selectable.Selectable
import wee.digital.sample.ui.fragment.dialog.selectable.SelectableArg
import wee.digital.sample.ui.fragment.dialog.selectable.SelectableFragment
import wee.digital.widget.custom.InputView
import wee.digital.widget.extension.ViewClickListener

interface MainFragmentView : FragmentView {

    val mainActivity: MainActivity?
    val self get() = this
    val mainVM: MainVM
    val dialogVM: DialogVM

    fun showProgress() = Unit

    fun hideProgress() = Unit

    var InputView.selectedItem: Selectable?
        get() = dialogVM.selectableMap[id]?.value
        set(value) {
            dialogVM.selectableMap[id]?.value = value
            text = value?.text
        }

    fun InputView.attachSelectableList(block: SelectableArg.() -> Unit) {
        val arg = SelectableArg()
        arg.key = this.id
        arg.selectedItem = this.selectedItem
        arg.block()
        val liveData = MutableLiveData(arg.selectedItem)
        liveData.observe {
            this.error = null
            this.text = it?.text
            it ?: return@observe
            arg.onItemSelected?.invoke(it)
        }
        dialogVM.selectableMap[this.id] = liveData
        setOnClickListener(object : ViewClickListener() {
            override fun onClicks(v: View) {
                dialogVM.selectableLiveData.value = arg
                show(SelectableFragment())
            }
        })
    }

}