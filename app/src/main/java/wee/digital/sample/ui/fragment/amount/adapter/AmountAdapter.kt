package wee.digital.sample.ui.fragment.amount.adapter

import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.WalletAmountMessageItemBinding
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions

class AmountAdapter : BaseRecyclerAdapter<AmountItem>() {
    override fun modelItemOptions(item: AmountItem?, position: Int): ItemOptions? {
        return ItemOptions(
            R.layout.wallet_amount_message_item,
            WalletAmountMessageItemBinding::bind
        )
    }

    override fun ViewBinding.onBindItem(item: AmountItem, position: Int) {
        if (this is WalletAmountMessageItemBinding) {
            tvPrice.text =item.amount
            viewUsername.text=item.user
        }
    }

}