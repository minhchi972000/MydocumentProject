package wee.digital.sample.ui.fragment.detail.phone

import android.view.View
import wee.digital.sample.databinding.FragmentBillPhoneBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.fragment.detail.phone.adapter.BillHomeNetworkAdapter
import wee.digital.sample.ui.main.MainFragment
import wee.digital.sample.ui.fragment.detail.phone.adapter.BillRecentPhoneNumberAdapter

class BillPhoneFragment : MainFragment<FragmentBillPhoneBinding>(), IBillPhoneView {

    override val homeNetworkAdapter = BillHomeNetworkAdapter()
    override val recentPhoneNumberAdapter = BillRecentPhoneNumberAdapter()

    override fun inflating(): Inflating = FragmentBillPhoneBinding::inflate

    override fun onViewCreated() {
        with(vb) {
            initView()
            addClickListener()
        }
    }

    override fun onViewClick(v: View?) {
        with(vb) {
            when (v?.id) {
            }
        }
    }

}