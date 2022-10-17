package wee.digital.sample.ui.fragment.detail.phone.adapter

import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.BillingItemRecentPhoneNumberBinding
import wee.digital.sample.ui.fragment.detail.phone.BillPhoneItem
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions

class BillRecentPhoneNumberAdapter : BaseRecyclerAdapter<BillPhoneItem>() {

    override fun modelItemOptions(item: BillPhoneItem?, position: Int): ItemOptions? {
        return ItemOptions(
            R.layout.billing_item_recent_phone_number, BillingItemRecentPhoneNumberBinding::bind
        )
    }

    override fun ViewBinding.onBindItem(item: BillPhoneItem, position: Int) {
        if (this !is BillingItemRecentPhoneNumberBinding) return
        //Bind data
        with(viewInfo) {
//            setTextTitle(item.homeNetwork)
//            setTextContent(item.phoneNumber)
//            setOnClickListener {
//                onItemClick?.invoke(item)
//            }
            text=item.phoneNumber
        }
        viewLine.isVisible = if (currentList.size > 1) {
            position < currentList.size - 1
        } else {
            false
        }
    }
}