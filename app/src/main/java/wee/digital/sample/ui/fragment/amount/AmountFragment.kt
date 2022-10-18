package wee.digital.sample.ui.fragment.amount

import androidx.recyclerview.widget.RecyclerView
import wee.digital.library.extension.toast
import wee.digital.sample.databinding.WalletAmountBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.fragment.amount.adapter.AmountAdapter
import wee.digital.sample.ui.main.MainFragment
import wee.digital.widget.extension.integerCash

class AmountFragment : MainFragment<WalletAmountBinding>() {
    val vm by lazyActivityVM(CashInVM::class)
    var adapter = QuickPickCashAdapter()
    var adapterAmount = AmountAdapter()
    override fun inflating(): Inflating = WalletAmountBinding::inflate

    override fun onViewCreated() {
        adapter.bind(vb.viewListQuickPick) {
            orientation = RecyclerView.HORIZONTAL
        }.set(vm.listQuickCash).onItemClick = {
            vb.viewInput.text = it.cash.toString().integerCash()
        }

        adapterAmount.bind(vb.viewTransferAmount) {
            orientation = RecyclerView.HORIZONTAL
        }
            .set(vm.listBankName)
            .onItemClick = {
                toast("${it.user}")
        }
    }
}