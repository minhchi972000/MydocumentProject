package wee.digital.sample.ui.fragment.home

import wee.digital.sample.shared.Pref
import wee.digital.sample.ui.base.BaseVM

class HomeVM : BaseVM() {

    var listBankName = mutableListOf<OptionModel>()
    fun logoutUser() {
        Pref.user = null
    }

    init {
        //add dummy data
        listBankName.add(OptionModel("TCB", OptionModel.ITEM_BANK_NAME, "Techcombank"))
        listBankName.add(OptionModel("SCB", OptionModel.ITEM_BANK_NAME, "Sacombank"))
        listBankName.add(OptionModel("VCB", OptionModel.ITEM_BANK_NAME, "Vietcombank"))
        listBankName.add(OptionModel("VTB", OptionModel.ITEM_BANK_NAME, "Vietinbank"))
        listBankName.add(OptionModel("OCB", OptionModel.ITEM_BANK_NAME, "OCB Bank"))
    }
}