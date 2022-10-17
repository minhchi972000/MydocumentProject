package wee.digital.widget.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import wee.digital.widget.extension.addClickListener

abstract class BaseSelectableAdapter<T, VB : ViewBinding> : BaseRecyclerAdapter<T>() {

    final override var onItemClick: OnItemClick<T> = { item ->
        updateSelectedItem(item)
    }

    final override fun modelItemOptions(item: T?, position: Int): ItemOptions? = null

    final override fun ViewBinding.onBindItem(item: T, position: Int) = Unit

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return ViewBindingHolder(itemInflating().invokeItem(parent))
    }

    final override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (viewHolder as? ViewBindingHolder<VB>)?.apply {
            val item = get(position) ?: return
            vb.onBindDefaultItem(item, position)
            if (isSelected(item)) {
                vb.onBindSelectedItem(item, position)
            } else {
                vb.onBindUnselectedItem(item, position)
            }
            vb.root.addClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    abstract fun itemInflating(): InflaterInvokerBinding<VB>

    abstract fun VB.onBindDefaultItem(item: T, position: Int)

    abstract fun VB.onBindSelectedItem(item: T, position: Int)

    abstract fun VB.onBindUnselectedItem(item: T, position: Int)

    open fun isSelected(item: T?): Boolean {
        return areSameItems(item, selectedItem)
    }

    open fun areSameItems(item: T?, other: T?): Boolean {
        return item == other
    }

    private var mSelectedItem: T? = null

    var selectedItem: T?
        get() = mSelectedItem
        private set(value) {
            if (!areSameItems(value, mSelectedItem) && dataNotEmpty) {
                notifyDataSetChanged()
            }
            mSelectedItem = value
        }

    val selectedPosition: Int
        get() {
            selectedItem ?: return -1
            return itemList().indexOf(selectedItem)
        }

    private val itemClickList = mutableListOf<(T, Boolean) -> Unit>()

    var onSelectionChanged: OnItemClick<T> = null

    fun onSelectionChanged(block: (T) -> Unit): BaseSelectableAdapter<T, VB> {
        onSelectionChanged = block
        return this
    }

    fun addOnItemClick(block: (T, Boolean/*hasChange*/) -> Unit): BaseSelectableAdapter<T, VB> {
        itemClickList.add(block)
        return this
    }

    fun select(item: T?, hasNotify: Boolean = true) {
        if (hasNotify) {
            selectedItem = item
        } else {
            mSelectedItem = item
        }
    }

    private fun updateSelectedItem(item: T) {
        if (isSelected(item)) {
            itemClickList.forEach { it(item, false) }
            return
        }
        selectedItem = item
        notifyDataSetChanged()
        onSelectionChanged?.invoke(item)
        itemClickList.forEach { it(item, true) }
    }

}