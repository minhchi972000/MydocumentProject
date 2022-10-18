package wee.digital.sample.ui.fragment.amount

import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.QuickPickCashItemBinding
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions
import wee.digital.widget.extension.integerCash

class QuickPickCashAdapter : BaseRecyclerAdapter<QuickPickCash>() {
    override fun modelItemOptions(item: QuickPickCash?, position: Int): ItemOptions? {
        return ItemOptions(R.layout.quick_pick_cash_item, QuickPickCashItemBinding::bind)
    }

    override fun ViewBinding.onBindItem(item: QuickPickCash, position: Int) {
        if (this is QuickPickCashItemBinding) {
            viewText.text = item.cash.toString().integerCash()
        }
    }
}