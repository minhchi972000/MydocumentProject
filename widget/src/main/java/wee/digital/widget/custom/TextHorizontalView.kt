package wee.digital.widget.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.viewbinding.ViewBinding
import wee.digital.widget.R
import wee.digital.widget.custom.base.AppCustomView
import wee.digital.widget.databinding.TextHorizontalBinding
import wee.digital.widget.extension.isGone

class TextHorizontalView : AppCustomView<TextHorizontalBinding> {

    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return TextHorizontalBinding::inflate
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInitialize(context: Context, types: TypedArray) {
        title = types.title
        vb.textViewTitle.setTextColor(types.textColorHint(color(R.color.colorHint)))
        text = types.text
        vb.textViewProperty.setTextColor(types.textColor(color(R.color.colorTextDefault)))
        src = types.srcRes
    }

    @DrawableRes
    var src: Int = 0
        set(value) {
            vb.imageViewIcon.isGone(value <= 0)
            vb.imageViewIcon.setImageResource(value)
            field = value
        }

    var title: String? = null
        set(value) {
            vb.textViewTitle.text = value
            field = value
        }

    var text: String? = null
        set(value) {
            vb.textViewProperty.text = value
            field = value
        }


}