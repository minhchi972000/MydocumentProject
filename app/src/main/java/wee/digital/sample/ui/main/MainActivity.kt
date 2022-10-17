package wee.digital.sample.ui.main

import androidx.navigation.NavController
import androidx.navigation.findNavController
import wee.digital.sample.R
import wee.digital.sample.databinding.MainBinding
import wee.digital.sample.ui.base.BaseActivity
import wee.digital.sample.ui.base.Inflating


class MainActivity : BaseActivity<MainBinding>() {

    private val mainVM by lazyViewModel(MainVM::class)

    override fun activityNavController(): NavController? {
        return findNavController(R.id.mainFragment)
    }

    override fun inflating(): Inflating = MainBinding::inflate

    override fun onViewCreated() {
    }

    override fun onLiveDataObserve() {

    }


}






