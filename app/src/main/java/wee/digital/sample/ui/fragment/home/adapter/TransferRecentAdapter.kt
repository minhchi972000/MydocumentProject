package wee.digital.sample.ui.fragment.home.adapter

import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.OptionItemBinding
import wee.digital.sample.databinding.TransferUserListRecentItemBinding
import wee.digital.sample.ui.fragment.home.OptionModel
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_BANK_ACCOUNT
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_BANK_NAME
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_CARD_NUMBER
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_CONTACT
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_MY_PHONE
import wee.digital.sample.ui.fragment.home.OptionModel.Companion.ITEM_PHONE_INFO
import wee.digital.sample.ui.fragment.home.RecentUserInfo
import wee.digital.widget.adapter.BaseRecyclerAdapter
import wee.digital.widget.adapter.ItemOptions

class TransferRecentAdapter : BaseRecyclerAdapter<OptionModel>() {


    override fun modelItemOptions(item: OptionModel?, position: Int): ItemOptions? {

        if (item?.type == OptionModel.ITEM_USER) {
            return ItemOptions(
                (R.layout.transfer_user_list_recent_item),
                TransferUserListRecentItemBinding::bind
            )
        }
        return ItemOptions(R.layout.option_item, OptionItemBinding::bind)
    }


    override fun ViewBinding.onBindItem(item: OptionModel, position: Int) {
        if (this is OptionItemBinding) {
            when (item.type) {
                ITEM_PHONE_INFO -> {
                    viewOption.apply {
//                        setTextContent(item.phone)
//                        setTextTitle(item.userName)
//                        setIcon(
//                            icon = ContextCompat.getDrawable(
//                                root.context,
//                                R.drawable.ic_profile_blu
//                            ),
//                            tintIcon = ContextCompat.getColor(
//                                root.context,
//                                R.color.colorGrayscaleDisable
//                            ),
//                            bgIcon = null,
//                            colorStateList = getColorStateList(root.context, R.color.color_hint)
//                        )
                        text = item.userName
                    }
                    viewDecor.isVisible = true
                }
                ITEM_MY_PHONE -> {
                    viewOption.apply {
                        text = item.userName
                    }
                    viewDecor.isVisible = false
                }
                ITEM_CONTACT -> {
                    viewOption.apply {
                        text = item.userName
                    }
                    viewDecor.isVisible = false
                }
                ITEM_CARD_NUMBER -> {
                    viewOption.apply {
                        text = item.userName
                    }
                    viewDecor.isVisible = false
                }
                ITEM_BANK_NAME -> {
                    viewOption.apply {
                        text = item.userName
                    }
                    viewDecor.isVisible = false
                }
                ITEM_BANK_ACCOUNT -> {
                    viewOption.apply {
                        text = item.userName
                    }
                    viewDecor.isVisible = true
                }
                else -> {
                    viewDecor.isVisible = true
                    //viewOption.setTextContent(root.context.getString(R.string.choose_from_phone_book))
                }

            }
        } else {
            this as TransferUserListRecentItemBinding
            val adapter = TransferRecentUserAdapter()
            //TODO dummy data user recent list
            val dummy = mutableListOf<RecentUserInfo>()
            dummy.add(RecentUserInfo("MC"))
            dummy.add(RecentUserInfo("MC"))
            adapter.bind(viewRecentList).set(dummy)
            viewRecentList.layoutManager = GridLayoutManager(viewRecentList.context, 4)
        }
    }
}