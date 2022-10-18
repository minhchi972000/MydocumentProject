package wee.digital.sample.ui.fragment.home

import android.view.View
import wee.digital.library.extension.toast
import wee.digital.sample.R
import wee.digital.sample.databinding.HomeBinding
import wee.digital.sample.navigate.HomeGraph
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.fragment.dialog.alert.showAlert
import wee.digital.sample.ui.fragment.dialog.selectable.Selectable
import wee.digital.sample.ui.fragment.dialog.selectable.showSelectable
import wee.digital.sample.ui.fragment.home.adapter.TransferRecentAdapter
import wee.digital.sample.ui.main.MainFragment

class HomeFragment : MainFragment<HomeBinding>() {

    val vm by lazyActivityVM(HomeVM::class)

    private val adapter = TransferRecentAdapter()
    override fun inflating(): Inflating = HomeBinding::inflate

    override fun onViewCreated() {
        addClickListener(vb.viewAlert,vb.viewCamera, vb.viewSelectable,vb.viewAmount)

        adapter.bind(vb.viewListBank).set(vm.listBankName).onItemClick = {
            // childNavigate(HomeGraph.toReview) {
//                    setPopUpTo(R.id.reviewCashInFragment)
//                    setHorizontalAnim()
            toast("$it")
            mainNavigate(R.id.motionFragment)
        }

    }

    override fun onLiveDataObserve() {

    }

    override fun onViewClick(v: View?) {
        when (v) {
            vb.viewAlert -> {
                showAlert {
                    title = "Custom alert dialog"
                    message = "You can dismiss by touch outside"
                    acceptOnClick = {
                        toast("you has been click accept")
                        mainNavigate(R.id.motionFragment)
                    }
                }
            }
            vb.viewCamera -> {
                showAlert {
                    title = "Custom camera dialog"
                    message = "You can dismiss by touch outside"
                    acceptOnClick = {
                        mainNavigate(R.id.cameraFragment)
                    }
                }
            }
            vb.viewSelectable -> {
                showSelectable {
                    title = "Sample selectable dialog"
                    message = "Please select once item below"
                    selectedItem = dialogVM.selectedItem(vb.viewSelectable.id)
                    itemList = listOf(
                        Selectable("foo", text = "Foo item"),
                        Selectable("bar", text = "Bar item")
                    )
                    onItemSelected = {
                        toast("${it.text} has been selected")
                    }
                }
            }
            vb.viewAmount -> {
                showAlert {
                    title = "Custom amount dialog"
                    message = "You can dismiss by touch outside"
                    acceptOnClick = {
                        mainNavigate(R.id.amountFragment)
                    }
                }
            }
        }
    }

}