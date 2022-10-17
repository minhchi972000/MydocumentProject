package wee.digital.widget.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wee.digital.widget.R
import wee.digital.widget.extension.*

class AppTabLayout : ConstraintLayout {

    private var selectionBarView: View? = null

    var onCheckedChanged: ((View, Boolean) -> Unit)? = null

    var selectedView: View? = null
        private set

    private var viewPager2: ViewPager2? = null
    private var viewPager: ViewPager? = null
    private var isSilent = false

    var checkableIds: MutableList<Int>

    fun setSelectedView(value: View?, smoothScroll: Boolean = false) {
        if (value !is TextView) return
        val isChanged = selectedView?.id != value.id
        selectedView = value
        value.bold()
        value.textColorRes(R.color.color_black)
        this.children.iterator().forEach { unSelectedView ->
            if (unSelectedView is TextView && unSelectedView.id != value.id) {
                unSelectedView.regular()
                unSelectedView.textColorRes(R.color.colorHint)
            }
        }
        value.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Main) {
            delay(250)
            updateTabSelectView(value, smoothScroll)
        }
        if (!isSilent) {
            isSilent = true
            viewPager2?.setCurrentItem(checkableIds.indexOf(value.id), smoothScroll)
            viewPager?.setCurrentItem(checkableIds.indexOf(value.id), smoothScroll)
            isSilent = false
        }

        onCheckedChanged?.invoke(value, isChanged)

    }


    private var checkedChangeListener: OnClickListener? = null

    constructor(context: Context, attributes: AttributeSet? = null) : super(context, attributes) {
        addTabSelectionBar()
        checkableIds = mutableListOf()
        checkedChangeListener = object : ViewClickListener() {
            override fun onClicks(v: View) {
                setSelectedView(v, true)
            }
        }
    }

    private fun updateTabSelectView(selectedView: View, smoothScroll: Boolean = false) {
        if (smoothScroll) beginTransition(400) {
            updateTabConstraintSet(selectedView)
        } else editConstraint {
            updateTabConstraintSet(selectedView)
        }
    }

    private fun ConstraintSet.updateTabConstraintSet(selectedView: View) {
        val sId = selectionBarView?.id ?: return
        val height = dpToPx(2F).toInt()
        val extraWidth = dpToPx(2F).toInt()
        val width = selectedView.width + extraWidth
        constrainWidth(sId, width)
        constrainDefaultWidth(sId, width)
        constrainHeight(sId, height)
        constrainDefaultHeight(sId, height)
        setTranslationZ(sId, 1F)
        connect(sId, ConstraintSet.START, selectedView.id, ConstraintSet.START)
        connect(sId, ConstraintSet.BOTTOM, selectedView.id, ConstraintSet.BOTTOM)
    }

    override fun onViewAdded(view: View?) {
        super.onViewAdded(view)
        (view as? TextView)?.also {
            if (it.id == 0) it.id = View.generateViewId()
            checkableIds.add(it.id)
            it.setOnClickListener(checkedChangeListener)
        }
    }

    private fun addTabSelectionBar() {
        selectionBarView = View(context).also {
            it.setBackgroundResource(R.color.color_primary)
            it.id = View.generateViewId()
        }
        this.addView(selectionBarView)
        val height = dpToPx(2F).toInt()
        val sId = selectionBarView!!.id
        editConstraint {
            constrainWidth(sId, 1)
            constrainDefaultWidth(sId, 1)
            constrainHeight(sId, height)
            constrainDefaultHeight(sId, height)
            setTranslationZ(sId, 1F)
            connect(sId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(sId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
    }

    fun attach(v: ViewPager2) {
        isSilent = false
        viewPager2 = v
        v.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position !in 0..checkableIds.lastIndex) return
                if (!isSilent) {
                    isSilent = true
                    setSelectedView(findViewById(checkableIds[position]))
                    isSilent = false
                    viewPager2?.post {
                        val lp = viewPager2?.layoutParams
                        lp?.height = LinearLayout.LayoutParams.WRAP_CONTENT
                        viewPager2?.layoutParams = lp
                    }
                }

            }
        })
    }

    fun attach(v: ViewPager) {
        isSilent = false
        viewPager = v
        v.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position !in 0..checkableIds.lastIndex) return
                if (!isSilent) {
                    isSilent = true
                    setSelectedView(findViewById(checkableIds[position]))
                    isSilent = false
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

}