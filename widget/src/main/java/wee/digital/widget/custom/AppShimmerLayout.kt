package wee.digital.widget.custom

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import wee.digital.widget.R
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Shimmer is an Android library that provides an easy way to add a shimmer effect to any [ ]. It is useful as an unobtrusive loading indicator, and was originally
 * developed for Facebook Home.
 *
 *
 * Find more examples and usage instructions over at: facebook.github.io/shimmer-android
 */
open class AppShimmerLayout : ConstraintLayout {

    private val mContentPaint = Paint()
    private val mShimmerDrawable = ShimmerDrawable()

    /** Return whether the shimmer drawable is visible.  */
    private var isShimmerVisible = true


    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        when (context) {
            is androidx.appcompat.view.ContextThemeWrapper,
            is android.view.ContextThemeWrapper,
            is AppCompatActivity -> {
                initShimmer(context, attrs)
            }
        }

    }

    private fun initShimmer(context: Context, attrs: AttributeSet?) {
        setWillNotDraw(false)
        mShimmerDrawable.callback = this
        if (attrs == null) {
            setShimmer(Shimmer.AlphaHighlightBuilder().build())
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout, 0, 0)
        try {
            val shimmerBuilder = if (a.hasValue(R.styleable.ShimmerLayout_shimmer_colored)
                && a.getBoolean(R.styleable.ShimmerLayout_shimmer_colored, false)
            ) Shimmer.ColorHighlightBuilder() else Shimmer.AlphaHighlightBuilder()
            setShimmer(shimmerBuilder.consumeAttributes(a)?.build())
        } finally {
            a.recycle()
        }
    }

    fun setShimmer(shimmer: Shimmer?): AppShimmerLayout {
        mShimmerDrawable.setShimmer(shimmer)
        if (shimmer != null && shimmer.clipToChildren) {
            setLayerType(LAYER_TYPE_HARDWARE, mContentPaint)
        } else {
            setLayerType(LAYER_TYPE_NONE, null)
        }
        return this
    }

    /** Starts the shimmer animation.  */
    fun startShimmer() {
        mShimmerDrawable.startShimmer()
    }

    /** Stops the shimmer animation.  */
    fun stopShimmer() {
        mShimmerDrawable.stopShimmer()
    }

    /** Return whether the shimmer animation has been started.  */
    val isShimmerStarted: Boolean
        get() = mShimmerDrawable.isShimmerStarted

    /**
     * Sets the ShimmerDrawable to be visible.
     * @param startShimmer Whether to start the shimmer again.
     */
    fun showShimmer(startShimmer: Boolean) {
        if (isShimmerVisible) {
            return
        }
        isShimmerVisible = true
        if (startShimmer) {
            startShimmer()
        }
    }

    /** Sets the ShimmerDrawable to be invisible, stopping it in the process.  */
    fun hideShimmer() {
        if (!isShimmerVisible) {
            return
        }
        stopShimmer()
        isShimmerVisible = false
        invalidate()
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = width
        val height = height
        mShimmerDrawable.setBounds(0, 0, width, height)
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mShimmerDrawable.maybeStartShimmer()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isShimmerVisible) {
            mShimmerDrawable.draw(canvas)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mShimmerDrawable
    }

    class Shimmer internal constructor() {
        /** The shape of the shimmer's highlight. By default LINEAR is used.  */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(Shape.LINEAR, Shape.RADIAL)
        annotation class Shape {
            companion object {
                const val LINEAR = 0
                const val RADIAL = 1
            }
        }

        /** Direction of the shimmer's sweep.  */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            Direction.LEFT_TO_RIGHT,
            Direction.TOP_TO_BOTTOM,
            Direction.RIGHT_TO_LEFT,
            Direction.BOTTOM_TO_TOP
        )
        annotation class Direction {
            companion object {
                const val LEFT_TO_RIGHT = 0
                const val TOP_TO_BOTTOM = 1
                const val RIGHT_TO_LEFT = 2
                const val BOTTOM_TO_TOP = 3
            }
        }

        val positions = FloatArray(COMPONENT_COUNT)
        val colors = IntArray(COMPONENT_COUNT)
        private val bounds = RectF()

        @Direction
        var direction = Direction.LEFT_TO_RIGHT

        @ColorInt
        var highlightColor = Color.WHITE

        @ColorInt
        var baseColor = 0x4cffffff

        @Shape
        var shape = Shape.LINEAR
        var fixedWidth = 0
        var fixedHeight = 0
        var widthRatio = 1f
        var heightRatio = 1f
        var intensity = 0f
        var dropoff = 0.5f
        var tilt = 20f
        var clipToChildren = true
        var autoStart = true
        var alphaShimmer = true
        var repeatCount = ValueAnimator.INFINITE
        var repeatMode = ValueAnimator.RESTART
        var animationDuration = 1000L
        var repeatDelay: Long = 0
        fun width(width: Int): Int {
            return if (fixedWidth > 0) fixedWidth else (widthRatio * width).roundToInt()
        }

        fun height(height: Int): Int {
            return if (fixedHeight > 0) fixedHeight else Math.round(heightRatio * height)
        }

        fun updateColors() {
            when (shape) {
                Shape.LINEAR -> {
                    colors[0] = baseColor
                    colors[1] = highlightColor
                    colors[2] = highlightColor
                    colors[3] = baseColor
                }
                Shape.RADIAL -> {
                    colors[0] = highlightColor
                    colors[1] = highlightColor
                    colors[2] = baseColor
                    colors[3] = baseColor
                }
                else -> {
                    colors[0] = baseColor
                    colors[1] = highlightColor
                    colors[2] = highlightColor
                    colors[3] = baseColor
                }
            }
        }

        fun updatePositions() {
            when (shape) {
                Shape.LINEAR -> {
                    positions[0] = Math.max((1f - intensity - dropoff) / 2f, 0f)
                    positions[1] = Math.max((1f - intensity - 0.001f) / 2f, 0f)
                    positions[2] = Math.min((1f + intensity + 0.001f) / 2f, 1f)
                    positions[3] = Math.min((1f + intensity + dropoff) / 2f, 1f)
                }
                Shape.RADIAL -> {
                    positions[0] = 0f
                    positions[1] = Math.min(intensity, 1f)
                    positions[2] = Math.min(intensity + dropoff, 1f)
                    positions[3] = 1f
                }
                else -> {
                    positions[0] = Math.max((1f - intensity - dropoff) / 2f, 0f)
                    positions[1] = Math.max((1f - intensity - 0.001f) / 2f, 0f)
                    positions[2] = Math.min((1f + intensity + 0.001f) / 2f, 1f)
                    positions[3] = Math.min((1f + intensity + dropoff) / 2f, 1f)
                }
            }
        }

        fun updateBounds(viewWidth: Int, viewHeight: Int) {
            val magnitude = Math.max(viewWidth, viewHeight)
            val rad = Math.PI / 2f - Math.toRadians((tilt % 90f).toDouble())
            val hyp = magnitude / Math.sin(rad)
            val padding = 3 * Math.round((hyp - magnitude).toFloat() / 2f)
            bounds[-padding.toFloat(), -padding.toFloat(), (width(viewWidth) + padding).toFloat()] =
                (height(viewHeight) + padding).toFloat()
        }

        abstract class Builder<T : Builder<T>?> {
            val mShimmer = Shimmer()

            // Gets around unchecked cast
            protected abstract val self: T

            /** Applies all specified options from the [AttributeSet].  */
            fun consumeAttributes(context: Context, attrs: AttributeSet?): T {
                val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout, 0, 0)
                return consumeAttributes(a)
            }

            open fun consumeAttributes(a: TypedArray): T {
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_clip_to_children)) {
                    setClipToChildren(
                        a.getBoolean(
                            R.styleable.ShimmerLayout_shimmer_clip_to_children,
                            mShimmer.clipToChildren
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_auto_start)) {
                    setAutoStart(
                        a.getBoolean(
                            R.styleable.ShimmerLayout_shimmer_auto_start,
                            mShimmer.autoStart
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_base_alpha)) {
                    setBaseAlpha(a.getFloat(R.styleable.ShimmerLayout_shimmer_base_alpha, 0.3f))
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_highlight_alpha)) {
                    setHighlightAlpha(
                        a.getFloat(
                            R.styleable.ShimmerLayout_shimmer_highlight_alpha,
                            1f
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_duration)) {
                    setDuration(
                        a.getInt(
                            R.styleable.ShimmerLayout_shimmer_duration,
                            mShimmer.animationDuration.toInt()
                        ).toLong()
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_repeat_count)) {
                    setRepeatCount(
                        a.getInt(
                            R.styleable.ShimmerLayout_shimmer_repeat_count,
                            mShimmer.repeatCount
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_repeat_delay)) {
                    setRepeatDelay(
                        a.getInt(
                            R.styleable.ShimmerLayout_shimmer_repeat_delay,
                            mShimmer.repeatDelay.toInt()
                        ).toLong()
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_repeat_mode)) {
                    setRepeatMode(
                        a.getInt(R.styleable.ShimmerLayout_shimmer_repeat_mode, mShimmer.repeatMode)
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_direction)) {
                    val direction =
                        a.getInt(R.styleable.ShimmerLayout_shimmer_direction, mShimmer.direction)
                    when (direction) {
                        Direction.LEFT_TO_RIGHT -> setDirection(Direction.LEFT_TO_RIGHT)
                        Direction.TOP_TO_BOTTOM -> setDirection(Direction.TOP_TO_BOTTOM)
                        Direction.RIGHT_TO_LEFT -> setDirection(Direction.RIGHT_TO_LEFT)
                        Direction.BOTTOM_TO_TOP -> setDirection(Direction.BOTTOM_TO_TOP)
                        else -> setDirection(Direction.LEFT_TO_RIGHT)
                    }
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_shape)) {
                    val shape = a.getInt(R.styleable.ShimmerLayout_shimmer_shape, mShimmer.shape)
                    when (shape) {
                        Shape.LINEAR -> setShape(Shape.LINEAR)
                        Shape.RADIAL -> setShape(Shape.RADIAL)
                        else -> setShape(Shape.LINEAR)
                    }
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_dropoff)) {
                    setDropoff(
                        a.getFloat(
                            R.styleable.ShimmerLayout_shimmer_dropoff,
                            mShimmer.dropoff
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_fixed_width)) {
                    setFixedWidth(
                        a.getDimensionPixelSize(
                            R.styleable.ShimmerLayout_shimmer_fixed_width, mShimmer.fixedWidth
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_fixed_height)) {
                    setFixedHeight(
                        a.getDimensionPixelSize(
                            R.styleable.ShimmerLayout_shimmer_fixed_height, mShimmer.fixedHeight
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_intensity)) {
                    setIntensity(
                        a.getFloat(R.styleable.ShimmerLayout_shimmer_intensity, mShimmer.intensity)
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_width_ratio)) {
                    setWidthRatio(
                        a.getFloat(
                            R.styleable.ShimmerLayout_shimmer_width_ratio,
                            mShimmer.widthRatio
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_height_ratio)) {
                    setHeightRatio(
                        a.getFloat(
                            R.styleable.ShimmerLayout_shimmer_height_ratio,
                            mShimmer.heightRatio
                        )
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_tilt)) {
                    setTilt(a.getFloat(R.styleable.ShimmerLayout_shimmer_tilt, mShimmer.tilt))
                }
                return self
            }

            /** Copies the configuration of an already built Shimmer to this builder  */
            fun copyFrom(other: Shimmer): T {
                setDirection(other.direction)
                setShape(other.shape)
                setFixedWidth(other.fixedWidth)
                setFixedHeight(other.fixedHeight)
                setWidthRatio(other.widthRatio)
                setHeightRatio(other.heightRatio)
                setIntensity(other.intensity)
                setDropoff(other.dropoff)
                setTilt(other.tilt)
                setClipToChildren(other.clipToChildren)
                setAutoStart(other.autoStart)
                setRepeatCount(other.repeatCount)
                setRepeatMode(other.repeatMode)
                setRepeatDelay(other.repeatDelay)
                setDuration(other.animationDuration)
                mShimmer.baseColor = other.baseColor
                mShimmer.highlightColor = other.highlightColor
                return self
            }

            /** Sets the direction of the shimmer's sweep. See [Shimmer.Direction].  */
            fun setDirection(@Direction direction: Int): T {
                mShimmer.direction = direction
                return self
            }

            /** Sets the shape of the shimmer. See [Shimmer.Shape].  */
            fun setShape(@Shape shape: Int): T {
                mShimmer.shape = shape
                return self
            }

            /** Sets the fixed width of the shimmer, in pixels.  */
            fun setFixedWidth(@Px fixedWidth: Int): T {
                require(fixedWidth >= 0) { "Given invalid width: $fixedWidth" }
                mShimmer.fixedWidth = fixedWidth
                return self
            }

            /** Sets the fixed height of the shimmer, in pixels.  */
            fun setFixedHeight(@Px fixedHeight: Int): T {
                require(fixedHeight >= 0) { "Given invalid height: $fixedHeight" }
                mShimmer.fixedHeight = fixedHeight
                return self
            }

            /** Sets the width ratio of the shimmer, multiplied against the total width of the layout.  */
            fun setWidthRatio(widthRatio: Float): T {
                require(widthRatio >= 0f) { "Given invalid width ratio: $widthRatio" }
                mShimmer.widthRatio = widthRatio
                return self
            }

            /** Sets the height ratio of the shimmer, multiplied against the total height of the layout.  */
            fun setHeightRatio(heightRatio: Float): T {
                require(heightRatio >= 0f) { "Given invalid height ratio: $heightRatio" }
                mShimmer.heightRatio = heightRatio
                return self
            }

            /** Sets the intensity of the shimmer. A larger value causes the shimmer to be larger.  */
            fun setIntensity(intensity: Float): T {
                require(intensity >= 0f) { "Given invalid intensity value: $intensity" }
                mShimmer.intensity = intensity
                return self
            }

            /**
             * Sets how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
             */
            fun setDropoff(dropoff: Float): T {
                require(dropoff >= 0f) { "Given invalid dropoff value: $dropoff" }
                mShimmer.dropoff = dropoff
                return self
            }

            /** Sets the tilt angle of the shimmer in degrees.  */
            fun setTilt(tilt: Float): T {
                mShimmer.tilt = tilt
                return self
            }

            /**
             * Sets the base alpha, which is the alpha of the underlying children, amount in the range [0,
             * 1].
             */
            fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
                val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
                mShimmer.baseColor = intAlpha shl 24 or (mShimmer.baseColor and 0x00FFFFFF)
                return self
            }

            /** Sets the shimmer alpha amount in the range [0, 1].  */
            fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
                val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
                mShimmer.highlightColor =
                    intAlpha shl 24 or (mShimmer.highlightColor and 0x00FFFFFF)
                return self
            }

            /**
             * Sets whether the shimmer will clip to the childrens' contents, or if it will opaquely draw on
             * top of the children.
             */
            fun setClipToChildren(status: Boolean): T {
                mShimmer.clipToChildren = status
                return self
            }

            /** Sets whether the shimmering animation will start automatically.  */
            fun setAutoStart(status: Boolean): T {
                mShimmer.autoStart = status
                return self
            }

            /**
             * Sets how often the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatCount].
             */
            fun setRepeatCount(repeatCount: Int): T {
                mShimmer.repeatCount = repeatCount
                return self
            }

            /**
             * Sets how the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatMode].
             */
            fun setRepeatMode(mode: Int): T {
                mShimmer.repeatMode = mode
                return self
            }

            /** Sets how long to wait in between repeats of the shimmering animation.  */
            fun setRepeatDelay(millis: Long): T {
                require(millis >= 0) { "Given a negative repeat delay: $millis" }
                mShimmer.repeatDelay = millis
                return self
            }

            /** Sets how long the shimmering animation takes to do one full sweep.  */
            fun setDuration(millis: Long): T {
                require(millis >= 0) { "Given a negative duration: $millis" }
                mShimmer.animationDuration = millis
                return self
            }

            fun build(): Shimmer {
                mShimmer.updateColors()
                mShimmer.updatePositions()
                return mShimmer
            }

            companion object {
                private fun clamp(min: Float, max: Float, value: Float): Float {
                    return max.coerceAtMost(min.coerceAtLeast(value))
                }
            }
        }

        open class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
            override val self: AlphaHighlightBuilder get() = this
        }

        class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
            /** Sets the highlight color for the shimmer.  */
            fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
                mShimmer.highlightColor = color
                return self
            }

            /** Sets the base color for the shimmer.  */
            fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
                mShimmer.baseColor = mShimmer.baseColor and -0x1000000 or (color and 0x00FFFFFF)
                return self
            }

            override fun consumeAttributes(a: TypedArray): ColorHighlightBuilder {
                super.consumeAttributes(a)
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_base_color)) {
                    setBaseColor(
                        a.getColor(R.styleable.ShimmerLayout_shimmer_base_color, mShimmer.baseColor)
                    )
                }
                if (a.hasValue(R.styleable.ShimmerLayout_shimmer_highlight_color)) {
                    setHighlightColor(
                        a.getColor(
                            R.styleable.ShimmerLayout_shimmer_highlight_color,
                            mShimmer.highlightColor
                        )
                    )
                }
                return self
            }

            override val self: ColorHighlightBuilder get() = this

            init {
                mShimmer.alphaShimmer = false
            }
        }

        companion object {
            private const val COMPONENT_COUNT = 4
        }
    }

    class ShimmerDrawable : Drawable() {
        private val mUpdateListener = ValueAnimator.AnimatorUpdateListener { invalidateSelf() }
        private val mShimmerPaint = Paint()
        private val mDrawRect = Rect()
        private val mShaderMatrix = Matrix()
        private var mValueAnimator: ValueAnimator? = null
        private var mShimmer: Shimmer? = null
        fun setShimmer(shimmer: Shimmer?) {
            mShimmer = shimmer
            if (mShimmer != null) {
                mShimmerPaint.xfermode = PorterDuffXfermode(
                    if (mShimmer!!.alphaShimmer) PorterDuff.Mode.DST_IN else PorterDuff.Mode.SRC_IN
                )
            }
            updateShader()
            updateValueAnimator()
            invalidateSelf()
        }

        /** Starts the shimmer animation.  */
        fun startShimmer() {
            if (mValueAnimator != null && !isShimmerStarted && callback != null) {
                mValueAnimator!!.start()
            }
        }

        /** Stops the shimmer animation.  */
        fun stopShimmer() {
            if (mValueAnimator != null && isShimmerStarted) {
                mValueAnimator!!.cancel()
            }
        }

        /** Return whether the shimmer animation has been started.  */
        val isShimmerStarted: Boolean
            get() = mValueAnimator != null && mValueAnimator!!.isStarted

        public override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            val width = bounds.width()
            val height = bounds.height()
            mDrawRect[0, 0, width] = height
            updateShader()
            maybeStartShimmer()
        }

        override fun draw(canvas: Canvas) {
            if (mShimmer == null || mShimmerPaint.shader == null) {
                return
            }
            val tiltTan = tan(Math.toRadians(mShimmer!!.tilt.toDouble()))
                .toFloat()
            val translateHeight = mDrawRect.height() + tiltTan * mDrawRect.width()
            val translateWidth = mDrawRect.width() + tiltTan * mDrawRect.height()
            val dx: Float
            val dy: Float
            val animatedValue =
                if (mValueAnimator != null) mValueAnimator!!.animatedFraction else 0f
            when (mShimmer!!.direction) {
                Shimmer.Direction.LEFT_TO_RIGHT -> {
                    dx = offset(-translateWidth, translateWidth, animatedValue)
                    dy = 0f
                }
                Shimmer.Direction.RIGHT_TO_LEFT -> {
                    dx = offset(translateWidth, -translateWidth, animatedValue)
                    dy = 0f
                }
                Shimmer.Direction.TOP_TO_BOTTOM -> {
                    dx = 0f
                    dy = offset(-translateHeight, translateHeight, animatedValue)
                }
                Shimmer.Direction.BOTTOM_TO_TOP -> {
                    dx = 0f
                    dy = offset(translateHeight, -translateHeight, animatedValue)
                }
                else -> {
                    dx = offset(-translateWidth, translateWidth, animatedValue)
                    dy = 0f
                }
            }
            mShaderMatrix.reset()
            mShaderMatrix.setRotate(
                mShimmer!!.tilt,
                mDrawRect.width() / 2f,
                mDrawRect.height() / 2f
            )
            mShaderMatrix.postTranslate(dx, dy)
            mShimmerPaint.shader.setLocalMatrix(mShaderMatrix)
            canvas.drawRect(mDrawRect, mShimmerPaint)
        }

        override fun setAlpha(alpha: Int) {
            // No-op, modify the Shimmer object you pass in instead
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            // No-op, modify the Shimmer object you pass in instead
        }

        override fun getOpacity(): Int {
            return if (mShimmer != null && (mShimmer!!.clipToChildren || mShimmer!!.alphaShimmer)) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
        }

        private fun offset(start: Float, end: Float, percent: Float): Float {
            return start + (end - start) * percent
        }

        private fun updateValueAnimator() {
            if (mShimmer == null) {
                return
            }
            val started: Boolean
            if (mValueAnimator != null) {
                started = mValueAnimator!!.isStarted
                mValueAnimator!!.cancel()
                mValueAnimator!!.removeAllUpdateListeners()
            } else {
                started = false
            }
            mValueAnimator = ValueAnimator.ofFloat(
                0f,
                1f + (mShimmer!!.repeatDelay / mShimmer!!.animationDuration).toFloat()
            ).apply {
                repeatMode = mShimmer!!.repeatMode
                repeatCount = mShimmer!!.repeatCount
                duration = mShimmer!!.animationDuration + mShimmer!!.repeatDelay
                addUpdateListener(mUpdateListener)
            }

            if (started) {
                mValueAnimator?.start()
            }
        }

        fun maybeStartShimmer() {
            if (mValueAnimator != null && !mValueAnimator!!.isStarted
                && mShimmer != null && mShimmer!!.autoStart
                && callback != null
            ) {
                mValueAnimator!!.start()
            }
        }

        private fun updateShader() {
            val bounds = bounds
            val boundsWidth = bounds.width()
            val boundsHeight = bounds.height()
            if (boundsWidth == 0 || boundsHeight == 0 || mShimmer == null) {
                return
            }
            val width = mShimmer!!.width(boundsWidth)
            val height = mShimmer!!.height(boundsHeight)
            val shader: Shader
            when (mShimmer!!.shape) {
                Shimmer.Shape.LINEAR -> {
                    val vertical = (mShimmer!!.direction == Shimmer.Direction.TOP_TO_BOTTOM
                            || mShimmer!!.direction == Shimmer.Direction.BOTTOM_TO_TOP)
                    val endX = if (vertical) 0 else width
                    val endY = if (vertical) height else 0
                    shader = LinearGradient(
                        0F,
                        0F,
                        endX.toFloat(),
                        endY.toFloat(),
                        mShimmer!!.colors,
                        mShimmer!!.positions,
                        Shader.TileMode.CLAMP
                    )
                }
                Shimmer.Shape.RADIAL -> shader = RadialGradient(
                    width / 2f,
                    height / 2f,
                    (width.coerceAtLeast(height) / sqrt(2.0)).toFloat(),
                    mShimmer!!.colors,
                    mShimmer!!.positions,
                    Shader.TileMode.CLAMP
                )
                else -> {
                    val vertical = (mShimmer!!.direction == Shimmer.Direction.TOP_TO_BOTTOM
                            || mShimmer!!.direction == Shimmer.Direction.BOTTOM_TO_TOP)
                    val endX = if (vertical) 0F else width
                    val endY = if (vertical) height else 0F
                    shader = LinearGradient(
                        0F,
                        0F,
                        endX.toFloat(),
                        endY.toFloat(),
                        mShimmer!!.colors,
                        mShimmer!!.positions,
                        Shader.TileMode.CLAMP
                    )
                }
            }
            mShimmerPaint.shader = shader
        }

        init {
            mShimmerPaint.isAntiAlias = true
        }
    }
}