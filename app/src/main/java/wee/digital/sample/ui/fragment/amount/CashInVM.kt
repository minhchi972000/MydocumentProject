package wee.digital.sample.ui.fragment.amount

import wee.digital.sample.ui.base.BaseVM
import wee.digital.sample.ui.fragment.amount.adapter.AmountItem
import wee.digital.sample.ui.fragment.home.OptionModel

class CashInVM: BaseVM() {
    var listQuickCash = mutableListOf<QuickPickCash>()
    var listBankName = mutableListOf<AmountItem>()

    init {
        //add dummy data
        listQuickCash.add(QuickPickCash(20000))
        listQuickCash.add(QuickPickCash(50000))
        listQuickCash.add(QuickPickCash(100000))
        listQuickCash.add(QuickPickCash(200000))
        listQuickCash.add(QuickPickCash(500000))
        listBankName.add(AmountItem("TCB", "200000"))
        listBankName.add(AmountItem("SCB",  "500000"))
        listBankName.add(AmountItem("VCB", "200000"))
        listBankName.add(AmountItem("VTB",  "200000"))
        listBankName.add(AmountItem("OCB",  "500000"))
    }
}