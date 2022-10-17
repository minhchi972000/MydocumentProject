package com.sample.widget.fragment

abstract class BasePagerInfinityAdapter<T> : BasePagerAdapter<T>() {

    override fun get(position: Int): T? {
        if (listItem.isEmpty()) return null
        return listItem[position % size]
    }

    override fun getCount(): Int {
        return listItem.size * 10000000
    }
}