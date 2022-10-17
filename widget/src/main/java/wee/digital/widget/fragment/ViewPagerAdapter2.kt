package wee.digital.widget.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

open class ViewPagerAdapter2 : FragmentStateAdapter {

    open var fragments = mutableListOf<Fragment>()

    constructor(fragment: Fragment) : super(
        fragment.childFragmentManager,
        fragment.viewLifecycleOwner.lifecycle
    )

    constructor(activity: AppCompatActivity) : super(
        activity.supportFragmentManager,
        activity.lifecycle
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = fragments[position]
        fragment.arguments = fragment.arguments?.apply {
            putInt("position", position + 1)
        }
        return fragment
    }

    fun set(position: Int, fragment: Fragment) {
        fragments.removeAt(position)
        fragments.add(position, fragment)
        notifyItemChanged(position)
    }

    fun add(vararg frags: Fragment): ViewPagerAdapter2 {
        fragments.addAll(frags)
        notifyDataSetChanged()
        return this
    }

    fun get(position: Int): Fragment? {
        return fragments.getOrNull(position)
    }
}