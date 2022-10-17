package wee.digital.sample.ui.fragment.dialog

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import wee.digital.sample.ui.base.BaseVM
import wee.digital.sample.ui.fragment.dialog.alert.AlertArg
import wee.digital.sample.ui.fragment.dialog.selectable.Selectable
import wee.digital.sample.ui.fragment.dialog.selectable.SelectableArg

class DialogVM : BaseVM() {

    val alertLiveData = MutableLiveData<AlertArg?>()

    val selectableMap = mutableMapOf<Int, MutableLiveData<Selectable?>>()

    val selectableLiveData = MutableLiveData<SelectableArg?>()

    var showDialogJob: Job? = null

    fun selectedItem(id:Int):Selectable?{
        return selectableMap[id]?.value
    }
}
