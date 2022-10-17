package wee.digital.sample.ui.fragment.home

data class OptionModel(
    var userName: String? = null,
    var type: Int? = null,
    var phone: String? = null
) {
    companion object {
        const val ITEM_RECENT: Int = 0
        const val ITEM_USER: Int = 1
        const val ITEM_PHONE_INFO: Int = 2
        const val ITEM_CARD_NUMBER: Int = 3
        const val ITEM_BANK_NAME: Int = 4
        const val ITEM_BANK_ACCOUNT: Int = 5
        const val ITEM_CONTACT: Int = 6
        const val ITEM_MY_PHONE: Int = 7
    }
}
