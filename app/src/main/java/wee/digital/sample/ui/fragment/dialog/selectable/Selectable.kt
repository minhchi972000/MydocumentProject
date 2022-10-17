package wee.digital.sample.ui.fragment.dialog.selectable

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import wee.digital.sample.R
import wee.digital.sample.ui.base.BaseView
import wee.digital.sample.ui.fragment.dialog.DialogVM
import wee.digital.widget.extension.color

open class Selectable(
    val id: String = "",
    var ic: Int = R.drawable.ic_check,
    var icColor: Int = color(R.color.color_primary),
    var icBackground: Int = Color.WHITE,
    val text: String? = null,
    val description: String? = null,
    var value: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        return (other as? Selectable)?.id == id
    }

    override fun toString(): String {
        return text ?: ""
    }
}


fun BaseView.showSelectable(block: SelectableArg.() -> Unit) {
    val vm = activityVM(DialogVM::class)
    val arg = SelectableArg()
    block?.invoke(arg)
    if (arg.itemList.isNullOrEmpty()) return
    vm.selectableLiveData.value = arg
    vm.selectableMap[arg.key] = MutableLiveData(arg.selectedItem).also {
        it.observe { selectable ->
            selectable ?: return@observe
            arg.onItemSelected?.invoke(selectable)
        }
    }
    vm.showDialogJob?.cancel()
    vm.showDialogJob = launch(300) {
       baseActivity?.show(SelectableFragment(),"selectable")
    }
}
