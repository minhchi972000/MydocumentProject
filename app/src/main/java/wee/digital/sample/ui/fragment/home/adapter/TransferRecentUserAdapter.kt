package wee.digital.sample.ui.fragment.home.adapter

import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.TransferUserRecentItemBinding
import wee.digital.sample.ui.fragment.home.RecentUserInfo
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions

class TransferRecentUserAdapter : BaseRecyclerAdapter<RecentUserInfo>() {
    override fun modelItemOptions(item: RecentUserInfo?, position: Int): ItemOptions? {
        return ItemOptions(
            (R.layout.transfer_user_recent_item),
            TransferUserRecentItemBinding::bind
        )
    }

    override fun ViewBinding.onBindItem(item: RecentUserInfo, position: Int) {
        if (this is TransferUserRecentItemBinding) {
            if (position != currentList.size - 1) {
                viewName.text = item.name
            } else {
                viewAvatar.setImageResource(R.drawable.ic_check)
                viewName.text = root.context.getString(R.string.app_name)
            }
        }
    }
}