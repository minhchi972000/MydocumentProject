package com.sample.widget.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewpager.widget.PagerAdapter

abstract class BasePagerAdapter<T> : PagerAdapter() {

    var selectedItem: T? = null

    var listItem: MutableList<T> = mutableListOf()

    val size: Int get() = listItem.size

    @LayoutRes
    abstract fun itemLayoutResource(): Int

    abstract fun View.onBind(model: T)

    open fun set(collection: List<T>?) {
        collection ?: return
        listItem.addAll(collection)
        notifyDataSetChanged()
    }

    open fun set(element: T?) {
        element ?: return
        listItem.add(element)
        notifyDataSetChanged()
    }

    open fun get(position: Int): T? {
        if (listItem.isEmpty())
            return null
        if (position !in 0 until listItem.size)
            return null
        return listItem[position]
    }

    open fun indexOf(element: T?): Int {
        element ?: return -1
        return listItem.indexOf(element)
    }

    open fun clear() {
        listItem = mutableListOf()
        notifyDataSetChanged()
    }

    open fun selectedPosition(): Int {
        selectedItem ?: return -1
        return indexOf(selectedItem)
    }

    override fun getCount(): Int {
        return size
    }

    override fun getItemPosition(obj: Any): Int {
        @Suppress("UNCHECKED_CAST")
        val position = listItem.indexOf(obj as T)
        return if (position >= 0) position else POSITION_NONE
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return obj == view
    }

    override fun instantiateItem(viewGroup: ViewGroup, position: Int): Any {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(itemLayoutResource(), viewGroup, false)
        view.onBind(listItem[position])
        viewGroup.addView(view, 0)
        return view
    }

    override fun destroyItem(viewGroup: ViewGroup, position: Int, obj: Any) {
        viewGroup.removeView(obj as View)
    }
}