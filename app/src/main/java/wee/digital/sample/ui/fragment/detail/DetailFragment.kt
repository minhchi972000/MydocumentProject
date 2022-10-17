package wee.digital.sample.ui.fragment.detail

import wee.digital.sample.databinding.DetailBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.main.MainFragment

class DetailFragment : MainFragment<DetailBinding>(){

    override fun inflating(): Inflating = DetailBinding::inflate
    override fun onViewCreated() {

    }
}