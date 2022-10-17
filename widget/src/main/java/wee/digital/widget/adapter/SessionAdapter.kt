package wee.digital.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import wee.digital.widget.extension.addClickListener


class SessionAdapter<T, VB : ViewBinding> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * [RecyclerView.Adapter] implement
     */
    override fun getItemCount(): Int {
        val size = currentList.size
        if (size == 0) return placeHolderItemCount
        if (size != 0 && footerBinding != null) return size + 1
        if (isInfinity) return size * 10000
        return size
    }

    override fun getItemViewType(position: Int): Int {
        val size = currentList.size
        return when {
            size == 0 -> PLACEHOLDER_TYPE
            size != 0 && position == size -> FOOTER
            else -> ITEM_TYPE
        }
    }

    private fun (((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)?).inflating(parent: ViewGroup): ViewBinding? {
        return this?.invoke(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            FOOTER -> footerBinding.inflating(parent)?.also {
                return ViewBindingHolder(it)
            }
            ITEM_TYPE, PLACEHOLDER_TYPE -> viewBinding.inflating(parent)?.also {
                return ViewBindingHolder(it)
            }
        }
        return GoneViewHolder(parent)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            FOOTER, PLACEHOLDER_TYPE -> return
        }
        @Suppress("UNCHECKED")
        (viewHolder as? ViewBindingHolder<VB>)?.also {
            viewHolder.vb.root.addClickListener {
                val item = currentList.getOrNull(viewHolder.absoluteAdapterPosition)
                    ?: return@addClickListener
                onItemClick?.invoke(item)
                onItemViewClick?.invoke(item, viewHolder.vb)
            }
            currentList.getOrNull(position % currentList.size)?.also { item ->
                onBindItem?.invoke(it.vb, item)
            }
        }
    }

    /**
     * [SessionAdapter] properties
     */
    private var currentList: MutableList<T> = mutableListOf()

    private var placeHolderItemCount: Int = 0

    fun placeHolderItemCount(count: Int): SessionAdapter<T, VB> {
        placeHolderItemCount = count
        return this
    }

    private var footerBinding: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)? = null

    fun footerBinding(block: (LayoutInflater, ViewGroup, Boolean) -> ViewBinding): SessionAdapter<T, VB> {
        footerBinding = block
        return this
    }

    private var viewBinding: ((LayoutInflater, ViewGroup, Boolean) -> VB)? = null

    fun viewBinding(block: (LayoutInflater, ViewGroup, Boolean) -> VB): SessionAdapter<T, VB> {
        viewBinding = block
        return this
    }

    private var onBindItem: ((VB, T) -> Unit)? = null

    fun onBindItem(block: (VB, T) -> Unit): SessionAdapter<T, VB> {
        onBindItem = block
        return this
    }

    private var onItemClick: ((T) -> Unit)? = null

    fun onItemClick(block: (T) -> Unit): SessionAdapter<T, VB> {
        onItemClick = block
        return this
    }

    private var onItemViewClick: ((T, VB) -> Unit)? = null

    fun onItemViewClick(block: (T, VB) -> Unit): SessionAdapter<T, VB> {
        onItemViewClick = block
        return this
    }

    private var isInfinity: Boolean = false

    fun isInfinity(bool: Boolean): SessionAdapter<T, VB> {
        isInfinity = bool
        return this
    }

    fun set(collection: Collection<T>?): SessionAdapter<T, VB> {
        currentList = collection?.toMutableList() ?: mutableListOf()
        notifyDataSetChanged()
        return this
    }

    fun setElseEmpty(collection: Collection<T>?): SessionAdapter<T, VB> {
        if (collection.isNullOrEmpty()) return this
        return set(collection)
    }

    fun bind(v: RecyclerView, lm: RecyclerView.LayoutManager): SessionAdapter<T, VB> {
        //v.itemAnimator = DefaultItemAnimator()
        (v.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        v.layoutManager = lm
        v.adapter = this
        return this
    }

    fun bind(
        v: RecyclerView,
        block: (LinearLayoutManager.() -> Unit)? = null
    ): SessionAdapter<T, VB> {
        val lm = CachingLinearLayoutManager(v.context)
        block?.invoke(lm)
        return bind(v, lm)
    }

    fun bind(
        v: RecyclerView,
        spanCount: Int,
        block: (GridLayoutManager.() -> Unit)? = null
    ): SessionAdapter<T, VB> {
        val lm = CachingGridLayoutManager(v.context, spanCount)
        block?.invoke(lm)
        return bind(v, lm)
    }

    companion object {
        const val PLACEHOLDER_TYPE = 0
        const val ITEM_TYPE = 1
        const val FOOTER = 2
    }

}