package wee.digital.widget.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    BaseAdapter<T, BaseRecyclerAdapter<T>> {

    /**
     * [PagingDataAdapter] implements
     */
    override fun getItemCount(): Int {
        return getBaseItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return getBaseItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return onBaseCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        onBaseBindViewHolder(viewHolder, position)
    }

    open fun <VB : ViewBinding> vb(
        parent: ViewGroup,
        viewType: Int
    ): InflaterInvokerBinding<VB>? = null

    /**
     *
     */
    protected var currentList: MutableList<T> = mutableListOf()

    override var onItemClick: OnItemClick<T> = null

    override var onItemViewClick: OnItemViewClick<T> = null

    override var onItemPositionClick: OnItemPositionClick<T> = null

    override var onItemViewLongClick: OnItemViewClick<T> = null

    override var onListChanged: (() -> Unit)? = null

    override var placeHolderItemCount: Int = 0

    override var onFooterIndexChanged: ((Int) -> Unit)? = null

    override var lastBindIndex: Int = -1

    override fun itemList(): MutableList<T> {
        return currentList
    }

    /**
     * Utils
     */
    open fun set(collection: Collection<T>?): BaseRecyclerAdapter<T> {
        currentList = collection?.toMutableList() ?: mutableListOf()
        lastBindIndex = -1
        notifyDataSetChanged()
        onListChanged()
        return this
    }

    open fun set(array: Array<T>?): BaseRecyclerAdapter<T> {
        currentList = array?.toMutableList() ?: mutableListOf()
        lastBindIndex = -1
        notifyDataSetChanged()
        onListChanged()
        return this
    }

    open fun setElseEmpty(collection: Collection<T>?): BaseRecyclerAdapter<T> {
        if (collection.isNullOrEmpty()) return this
        return set(collection)
    }

    open fun setElseEmpty(array: Array<T>?): BaseRecyclerAdapter<T> {
        if (array == null || array.isEmpty()) return this
        return set(array)
    }

    open fun setElseEmpties(vararg elements: T?): BaseRecyclerAdapter<T> {
        return setElseEmpty(elements.toList().filterNotNull())
    }

    open fun add(collection: Collection<T>?) {
        if (collection.isNullOrEmpty()) return
        val oldLastIndex = currentList.lastIndex
        currentList.addAll(collection)
        notifyItemRangeInserted(oldLastIndex, lastIndex)
        onListChanged()
    }

    open fun add(array: Array<T>?) {
        add(collection = array?.toList())
    }

    open fun add(model: T?) {
        model ?: return
        add(lastIndex, model)
    }

    open fun add(position: Int, model: T?) {
        model ?: return
        currentList.add(position, model)
        onListChanged()
        notifyDataSetChanged()
    }

    open fun edit(position: Int, model: T?) {
        model ?: return
        if (position in 0..lastIndex) {
            currentList[position] = model
            notifyItemChanged(position)
            onListChanged()
        }
    }

    open fun remove(index: Int) {
        currentList.removeAt(index)
        notifyItemRemoved(index)
        onListChanged()
    }

    open fun remove(model: T?) {
        model ?: return
        val position = currentList.indexOf(model)
        remove(position)
    }

    open fun clear() {
        currentList = mutableListOf()
        notifyDataSetChanged()
    }

}