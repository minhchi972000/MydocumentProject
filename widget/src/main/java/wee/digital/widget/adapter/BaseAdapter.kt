package wee.digital.widget.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import wee.digital.widget.extension.addClickListener

interface BaseAdapter<T, A : RecyclerView.Adapter<*>> {

    @Suppress("UNCHECKED_CAST")
    val recyclerViewAdapter: A
        get() = this as A

    /**
     * Item click
     */
    fun itemClickDelayed(): Long = 600

    var onItemClick: OnItemClick<T>

    fun onItemClick(block: OnItemClick<T>): A {
        onItemClick = block
        return recyclerViewAdapter
    }

    var onItemViewClick: OnItemViewClick<T>

    fun onItemViewClick(block: OnItemViewClick<T>): A {
        onItemViewClick = block
        return recyclerViewAdapter
    }

    fun <VB : ViewBinding> onItemClick(block: ((T, VB) -> Unit)? = null): A {
        onItemViewClick = { item, bind ->
            @Suppress("UNCHECKED_CAST")
            (bind as? VB)?.also { vb ->
                block?.invoke(item, vb)
            }
        }
        return recyclerViewAdapter
    }

    var onItemPositionClick: OnItemPositionClick<T>

    fun onItemPositionClick(block: OnItemPositionClick<T>): A {
        onItemPositionClick = block
        return recyclerViewAdapter
    }

    fun onItemViewClick(viewHolder: RecyclerView.ViewHolder, viewBinding: ViewBinding) {
        val position: Int = viewHolder.absoluteAdapterPosition
        val item = get(position) ?: return
        onItemClick?.invoke(item)
        onItemViewClick?.invoke(item, viewBinding)
        onItemPositionClick?.invoke(item, position)
    }

    /**
     * Item long click
     */
    var onItemViewLongClick: OnItemViewClick<T>

    fun onItemViewLongClick(block: OnItemViewClick<T>): A {
        onItemViewLongClick = block
        return recyclerViewAdapter
    }

    fun onItemViewLongClick(viewHolder: RecyclerView.ViewHolder, viewBinding: ViewBinding) {
        val position: Int = viewHolder.absoluteAdapterPosition
        val item = get(position) ?: return
        onItemViewLongClick?.invoke(item, viewBinding)
    }

    var onListChanged: (() -> Unit)?

    fun onListChanged() {
        onListChanged?.invoke()
    }

    /**
     *
     */
    fun itemList(): List<T>

    val size: Int get() = itemList().size

    val lastIndex: Int get() = itemList().lastIndex

    val dataIsEmpty: Boolean get() = itemList().isEmpty()

    val dataNotEmpty: Boolean get() = itemList().isNotEmpty()

    fun indexOf(item: T): Int {
        return itemList().indexOf(item)
    }

    fun get(position: Int): T? {
        if (dataIsEmpty) return null
        if (isInfinity()) return itemList().getOrNull(position % size)
        return itemList().getOrNull(position)
    }

    fun getBaseItemCount(): Int {
        if (dataIsEmpty && placeHolderItemCount() > 0) return placeHolderItemCount()
        if (dataIsEmpty && blankItemOptions() != null) return 1
        if (dataNotEmpty && footerItemOptions() != null) return size + 1
        if (isInfinity()) return size * 10000
        return size
    }

    fun getBaseItemViewType(position: Int): Int {
        blankItemOptions()?.also {
            if (dataIsEmpty) {
                return it.layoutId
            }
        }
        footerItemOptions()?.also {
            if (dataNotEmpty && position == size) {
                return it.layoutId
            }
        }
        val model = get(position)
        return modelItemOptions(model, position)?.layoutId ?: GONE_VIEW_TYPE
    }

    fun onBaseCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType != GONE_VIEW_TYPE) {
            return BaseViewHolder(parent = parent, layoutId = viewType)
        }
        return GoneViewHolder(parent)
    }

    fun onBaseBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val itemView: View = viewHolder.itemView
        val viewType: Int = viewHolder.itemViewType
        if (viewType == GONE_VIEW_TYPE) {
            return
        }
        blankItemOptions()?.also { it: ItemOptions ->
            if (viewType == it.layoutId) {
                it.inflaterInvoker(itemView).onBindBlankItem()
                return
            }
        }
        footerItemOptions()?.also { it: ItemOptions ->
            if (viewType == it.layoutId) {
                it.inflaterInvoker(itemView).onBindFooterItem()
                return
            }
        }
        val model: T = get(position) ?: return
        val options: ItemOptions = modelItemOptions(model, position) ?: return
        val viewBinding: ViewBinding = options.inflaterInvoker(itemView)
        itemView.addClickListener(itemClickDelayed(), 1) {
            onItemViewClick(viewHolder, viewBinding)
        }
        itemView.setOnLongClickListener {
            onItemViewLongClick(viewHolder, viewBinding)
            true
        }
        viewBinding.onBindItem(viewHolder, model, position)
        viewBinding.onBindItem(model, position)
        lastBindIndex = position
    }

    /**
     *
     */
    fun blankItemOptions(): ItemOptions? = null

    fun ViewBinding.onBindBlankItem() = Unit

    /**
     *
     */
    var onFooterIndexChanged: ((Int) -> Unit)?

    var lastBindIndex: Int

    val hasFooter get() = footerItemOptions() != null

    fun footerItemOptions(): ItemOptions? = null

    fun ViewBinding.onBindFooterItem() = Unit

    /**
     *
     */
    var placeHolderItemCount: Int

    fun placeHolderItemCount(): Int = placeHolderItemCount

    fun placeHolderItemCount(count: Int): A {
        placeHolderItemCount = count
        return recyclerViewAdapter
    }

    fun isInfinity(): Boolean = false

    /**
     *
     */
    fun modelItemOptions(item: T?, position: Int): ItemOptions?

    fun ViewBinding.onBindItem(vh: RecyclerView.ViewHolder, item: T, position: Int) = Unit

    fun ViewBinding.onBindItem(item: T, position: Int) = Unit

    /**
     *
     */
    fun bind(v: RecyclerView, lm: RecyclerView.LayoutManager): A {
        v.itemAnimator = DefaultItemAnimator()
        (v.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        v.layoutManager = lm
        v.adapter = this as RecyclerView.Adapter<*>
        return recyclerViewAdapter
    }

    fun bind(v: RecyclerView, block: (LinearLayoutManager.() -> Unit)? = null): A {
        val lm = LinearLayoutManager(v.context)
        block?.invoke(lm)
        return bind(v, lm)
    }

    fun bind(v: RecyclerView, spanCount: Int, block: (GridLayoutManager.() -> Unit)? = null): A {
        val lm = GridLayoutManager(v.context, spanCount)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (dataIsEmpty || position == size) lm.spanCount
                else 1
            }
        }
        block?.invoke(lm)
        return bind(v, lm)
    }

    companion object {
        const val GONE_VIEW_TYPE = 0
    }

}