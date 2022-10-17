package wee.digital.sample.ui.fragment.detail.phone.adapter

import androidx.core.view.isInvisible
import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.BillingItemHomeNetworkHrzBinding
import wee.digital.sample.ui.fragment.detail.phone.HomeNetworkItem
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions
import wee.digital.widget.extension.load

class BillHomeNetworkAdapter : BaseRecyclerAdapter<HomeNetworkItem>() {

    private var idxSelected = 0

    override fun modelItemOptions(item: HomeNetworkItem?, position: Int): ItemOptions? {
        return ItemOptions(
            R.layout.billing_item_home_network_hrz, BillingItemHomeNetworkHrzBinding::bind
        )
    }

    override fun ViewBinding.onBindItem(item: HomeNetworkItem, position: Int) {
        if (this !is BillingItemHomeNetworkHrzBinding) return
        if (item.isSelected) {
            idxSelected = position
        }
        //Bind data
        viewLineSelected.isInvisible = !item.isSelected
        item.logo?.let { viewLogo.load(it) }
        viewName.text = item.homeNetwork
        root.setOnClickListener {
            updateStateItem(item, position)
            onItemClick?.invoke(item)
        }
    }

    private fun updateStateItem(item: HomeNetworkItem, position: Int) {
        if (position != idxSelected) {
            currentList[idxSelected].isSelected = false
            notifyItemChanged(idxSelected)
            item.isSelected = true
            notifyItemChanged(position)
        }
    }
}