package wee.digital.sample.ui.fragment.detail.phone

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import wee.digital.library.extension.toast
import wee.digital.sample.R
import wee.digital.sample.databinding.FragmentBillPhoneBinding
import wee.digital.sample.ui.fragment.detail.phone.adapter.BillHomeNetworkAdapter
import wee.digital.sample.ui.main.MainFragmentView
import wee.digital.sample.ui.fragment.detail.phone.adapter.BillRecentPhoneNumberAdapter


interface IBillPhoneView : MainFragmentView {
    val homeNetworkAdapter: BillHomeNetworkAdapter
    val recentPhoneNumberAdapter: BillRecentPhoneNumberAdapter

    fun FragmentBillPhoneBinding.initView() {
        setupInputView()
        bindHomeNetworkAdapter()
        bindRecentPhoneNumberAdapter()
        showRecentPhoneNumber(true)
    }

    fun FragmentBillPhoneBinding.bindRecentPhoneNumberAdapter() {
        recentPhoneNumberAdapter.bind(recyclerViewRecentPhoneNumber).set(mutableListOf(
            BillPhoneItem("0", "0767006210", "Mobifone"),
            BillPhoneItem("1", "0913749180", "Mobifone"),
            BillPhoneItem("2", "0913749180", "Vinaphone"),
            BillPhoneItem("3", "0913749180", "Vinaphone"),
        )).onItemClick = {
            nextPage()
        }
    }

    fun FragmentBillPhoneBinding.bindHomeNetworkAdapter() {
        homeNetworkAdapter.bind(recyclerViewHomeNetwork) {
            orientation = LinearLayoutManager.HORIZONTAL
        }.set(
            mutableListOf(
                HomeNetworkItem("0", R.drawable.ic_check, "Mobifone", true),
                HomeNetworkItem("1", R.drawable.ic_checkbox, "Vinaphone")
            )
        ).onItemClick = {
            toast(it.homeNetwork)
        }
        recyclerViewHomeNetwork.itemAnimator = DefaultItemAnimator()
    }

    fun FragmentBillPhoneBinding.setupInputView() {
        with(viewInput) {
            src = R.drawable.ic_check
            //setupInputType(InputView.PHONE_NUMBER)
            title = context.getString(R.string.chat_title)
            maxLengths = 10
            onTextChanged = { text ->
               // setEnableButtonEnd(text.isPhoneNumber || text?.isEmpty() == false)
                showRecentPhoneNumber(text?.isEmpty() == true)
            }
            addButtonClick {
                launch {
                    //Fake
                    showProgress()
                    viewInput.error = "Số điện thoại không đúng, bạn vui lòng thử lại."
                    delay(1000)
                    hideProgress()

                    nextPage()
                }
            }
            requestFocus()
        }
    }

    fun FragmentBillPhoneBinding.showRecentPhoneNumber(isShow: Boolean) {
        textView.isVisible = isShow
        recyclerViewRecentPhoneNumber.isVisible = isShow
    }

    fun nextPage() {
        hideKeyboard()
      //  billingVM.nextStep.postValue(2)
       // childNavigate(BillingGraph.review)
    }
}