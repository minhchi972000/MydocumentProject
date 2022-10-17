package wee.digital.sample.ui.fragment.detail

import androidx.lifecycle.MutableLiveData
import wee.digital.sample.ui.base.BaseVM

class DetailVM : BaseVM() {
    var typeBilling = MutableLiveData<String?>()

}