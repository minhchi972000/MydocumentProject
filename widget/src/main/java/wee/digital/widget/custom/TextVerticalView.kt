package wee.digital.widget.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import wee.digital.widget.R
import wee.digital.widget.custom.base.AppCustomView
import wee.digital.widget.databinding.TextVerticalBinding
import wee.digital.widget.extension.addClickListener
import wee.digital.widget.extension.dpToPx
import wee.digital.widget.extension.isGone

class TextVerticalView : AppCustomView<TextVerticalBinding> {

    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return TextVerticalBinding::inflate
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInitialize(context: Context, types: TypedArray) {
        title = types.title
        vb.textViewTitle.setTextColor(types.textColor(color(R.color.colorTextDefault)))

        text = types.text
        vb.textViewProperty.setTextColor(types.textColorHint(color(R.color.colorHint)))

        val drawableTint = types.drawableTint(color(R.color.color_primary_light))
        vb.imageViewDrawableStart.backgroundTint(drawableTint)
        val tint = types.tint(Color.TRANSPARENT)
        if (tint != Color.TRANSPARENT) {
            vb.imageViewDrawableStart.tint(tint)
        }
        vb.imageViewDrawableStart.setImageDrawable(types.drawableStart)
        vb.imageViewDrawableEnd.setImageDrawable(types.drawableEnd)

        isChecked = types.checked
        vb.switchView.visibility = if (types.checkable) View.VISIBLE else View.GONE
        vb.switchView.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged?.invoke(
                isChecked
            )
        }

        backgroundColor =
            types.getColor(R.styleable.AppCustomView_backgroundColor, color(R.color.colorLight))
        strokeLineWidth =
            types.pixels(R.styleable.AppCustomView_strokeLineWidth, dpToPx(1F).toInt())
        strokeLineColor =
            types.getColor(R.styleable.AppCustomView_strokeLineColor, color(R.color.colorHint))

    }

    override fun setBackground(background: Drawable?) {
        if (background is ColorDrawable) {
            super.setBackground(null)
        } else {
            super.setBackground(background)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vb.textViewTitle.textColorRes(
            if (enabled) R.color.colorTextDefault
            else R.color.colorHint
        )
    }

    fun bindInfo(s: String?) {
        if (s.isNullOrEmpty()) {
            title = "Chưa có thông tin"
            titleColor = color(R.color.colorHint)
        } else {
            title = s
            titleColor = color(R.color.colorTextDefault)
        }
    }

    var isChecked: Boolean
        get() = vb.switchView.isChecked
        set(value) {
            vb.switchView.isChecked = value
        }

    var onCheckedChanged: ((Boolean) -> Unit)? = null

    var title: String? = null
        set(value) {
            vb.textViewTitle.isGone(value.isNullOrEmpty())
            vb.textViewTitle.text = value
            field = value
        }

    var titleColor: Int = 0
        set(value) {
            vb.textViewTitle.setTextColor(value)
            field = value
        }

    var titleColorRes: Int = 0
        set(value) {
            vb.textViewTitle.setTextColor(ContextCompat.getColor(context, value))
            field = value
        }

    var text: String? = null
        set(value) {
            vb.textViewPlaceHolder.isGone(value.isNullOrEmpty())
            vb.textViewProperty.isGone(value.isNullOrEmpty())
            vb.textViewProperty.text = value
            field = value
        }

    var onNavClick: (() -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                vb.imageViewDrawableEnd.addClickListener {
                    onNavClick?.invoke()
                }
            } else {
                vb.imageViewDrawableEnd.addClickListener {
                    this.performClick()
                }
            }
        }

    var tint: Int = 0
        set(value) {
            vb.imageViewDrawableStart.tint(value)
            field = value
        }

    @DrawableRes
    var drawableEndRes: Int = 0
        set(value) {
            vb.imageViewDrawableEnd.isGone(value <= 0)
            vb.imageViewDrawableEnd.setImageResource(value)
            field = value
        }

    @DrawableRes
    var drawableStartRes: Int = 0
        set(value) {
            vb.imageViewDrawableStart.isGone(value <= 0)
            vb.imageViewDrawableStart.setImageResource(value)
            field = value
        }

    var backgroundColor: Int?
        get() = vb.textContentLayout.backgroundColor
        set(value) {
            vb.textContentLayout.backgroundColor = value
        }

    var strokeLineWidth: Float
        get() = vb.textContentLayout.strokeLineWidth
        set(value) {
            vb.textContentLayout.strokeLineWidth = value
        }

    var strokeLineColor: Int
        get() = vb.textContentLayout.strokeLineColor
        set(@ColorInt value) {
            vb.textContentLayout.strokeLineColor = value
        }

    var maxLines: Int
        get() = vb.textViewTitle.maxLines
        set(value) {
            vb.textViewTitle.maxLines = value
            vb.textViewTitle.ellipsize = TextUtils.TruncateAt.END
        }


}