package wee.digital.widget.custom

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class TabBarRecyclerView : RecyclerView {
    var selectedPosition: Int = -1

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        superState?.let {
            val state = SaveState(superState)
            state.selectedPosition = (adapter as? SampleAdapter)?.selectedPosition ?: -1
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        when (state) {
            is SaveState -> {
                selectedPosition = state.selectedPosition
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        (adapter as? SampleAdapter)?.also { adapter ->
            if (selectedPosition > 0) {
                scrollToPosition(adapter.selectedPosition)
            }
        }
    }


    // cai adapter ma dung cho cai recycler view nay
    abstract class SampleAdapter : RecyclerView.Adapter<ViewHolder>() {
        var selectedPosition: Int = -1
    }

    inner class SaveState : AbsSavedState {
        var selectedPosition: Int = -1

        constructor(superState: Parcelable) : super(superState)

        @RequiresApi(Build.VERSION_CODES.N)
        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            selectedPosition = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            selectedPosition.also { dest?.writeInt(it) }
        }
    }
}