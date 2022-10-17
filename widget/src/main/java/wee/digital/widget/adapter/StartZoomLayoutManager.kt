package wee.digital.widget.adapter

import android.content.Context
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.abs

class StartZoomLayoutManager : LinearLayoutManager {

    private val mShrinkAmount = 0.15f
    private val mShrinkDistance = 0.9f

    constructor(
        context: Context?,
        orientation: Int = RecyclerView.HORIZONTAL,
        reverseLayout: Boolean = false
    ) : super(
        context,
        orientation,
        reverseLayout
    )

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val orientation = orientation
        return if (orientation == VERTICAL) {
            val scrolled = super.scrollVerticallyBy(dy, recycler, state)
            val midpoint = height / 2f
            val d0 = 0f
            val d1 = mShrinkDistance * midpoint
            val s0 = 1f
            val s1 = 1f - mShrinkAmount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val childMidpoint = (getDecoratedBottom(child!!) + getDecoratedTop(
                    child
                )) / 2f
                val d = d1.coerceAtMost(abs(midpoint - childMidpoint))
                val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
                child.scaleX = scale
                child.scaleY = scale
            }
            scrolled
        } else {
            0
        }
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val orientation = orientation
        return if (orientation == HORIZONTAL) {
            val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
            val midpoint = width / 2f
            val d0 = 0f
            val d1 = mShrinkDistance * midpoint
            val s0 = 1f
            val s1 = 1f - mShrinkAmount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val childMidpoint = (getDecoratedRight(child!!) + getDecoratedLeft(
                    child
                )) / 2f
                val d = d1.coerceAtMost(abs(midpoint - childMidpoint))
                val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
                child.pivotX = -0F
                child.scaleX = scale
                child.scaleY = scale
            }
            scrolled
        } else {
            0
        }
    }

    override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
        return 30
    }

    fun validate() {
        val midpoint = width / 2f
        val d0 = 0f
        val d1 = mShrinkDistance * midpoint
        val s0 = 1f
        val s1 = 1f - mShrinkAmount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childMidpoint = (getDecoratedRight(child!!) + getDecoratedLeft(
                child
            )) / 2f
            val d = d1.coerceAtMost(abs(midpoint - childMidpoint))
            val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
            child.pivotX = -30f
            child.scaleX = scale
            child.scaleY = scale
        }
    }

    class StartSnapHelper : LinearSnapHelper() {
        private var mVerticalHelper: OrientationHelper? = null
        private var mHorizontalHelper: OrientationHelper? = null

        @Throws(IllegalStateException::class)
        override fun attachToRecyclerView(@Nullable recyclerView: RecyclerView?) {
            recyclerView?.onFlingListener = null
            super.attachToRecyclerView(recyclerView)
        }

        override fun calculateDistanceToFinalSnap(
            layoutManager: RecyclerView.LayoutManager,
            targetView: View
        ): IntArray? {
            val out = IntArray(2)
            if (layoutManager.canScrollHorizontally()) {
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
            } else {
                out[0] = 0
            }
            if (layoutManager.canScrollVertically()) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
            } else {
                out[1] = 0
            }
            return out
        }

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
            return if (layoutManager is LinearLayoutManager) {
                if (layoutManager.canScrollHorizontally()) {
                    getStartView(layoutManager, getHorizontalHelper(layoutManager))
                } else {
                    getStartView(layoutManager, getVerticalHelper(layoutManager))
                }
            } else super.findSnapView(layoutManager)
        }

        private fun distanceToStart(targetView: View, helper: OrientationHelper?): Int {
            return helper!!.getDecoratedStart(targetView) - helper.startAfterPadding
        }

        private fun getStartView(
            layoutManager: RecyclerView.LayoutManager,
            helper: OrientationHelper?
        ): View? {
            if (layoutManager is LinearLayoutManager) {
                val firstChild = layoutManager.findFirstVisibleItemPosition()
                val isLastItem = (layoutManager
                    .findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1)
                if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                    return null
                }
                val child = layoutManager.findViewByPosition(firstChild)
                return if (helper!!.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                    && helper.getDecoratedEnd(child) > 0
                ) {
                    child
                } else {
                    if (layoutManager.findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1
                    ) {
                        null
                    } else {
                        layoutManager.findViewByPosition(firstChild + 1)
                    }
                }
            }
            return super.findSnapView(layoutManager)
        }

        private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
            if (mVerticalHelper == null) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
            }
            return mVerticalHelper
        }

        private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
            }
            return mHorizontalHelper
        }
    }

    companion object {

        fun attach(v: RecyclerView): StartZoomLayoutManager {
            StartSnapHelper().attachToRecyclerView(v)
            val lm = StartZoomLayoutManager(v.context)
            v.layoutManager = lm
            return lm
        }

    }
}