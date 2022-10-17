package wee.digital.sample.ui.main

import androidx.viewbinding.ViewBinding
import wee.digital.sample.ui.base.BaseFragment
import wee.digital.sample.ui.fragment.dialog.DialogVM

abstract class MainFragment<B : ViewBinding> : BaseFragment<B>(), MainFragmentView {

    override val mainActivity get() = requireActivity() as? MainActivity
    override val mainVM by lazyActivityVM(MainVM::class)
    override val dialogVM by lazyActivityVM(DialogVM::class)

}