package wee.digital.widget.extension

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.*
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager


fun View.animRotateAxisY(block: ObjectAnimator.() -> Unit): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, "rotationY", 0.0f, 360f).also {
        it.interpolator = AccelerateDecelerateInterpolator()
        it.block()
    }
}

fun View.animateAlpha(duration: Int, alpha: Float, onAnimationEnd: () -> Unit = {}) {
    clearAnimation()
    val anim = AlphaAnimation(this.alpha, alpha)
    anim.duration = duration.toLong()
    anim.fillAfter = true
    anim.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationStart(animation: Animation) {
            this@animateAlpha.alpha = 1f
        }

        override fun onAnimationEnd(animation: Animation) {
            this@animateAlpha.alpha = alpha
            onAnimationEnd()
        }
    })
    post {
        startAnimation(anim)
    }
}

fun View.animateAlpha(
    duration: Int,
    formAlpha: Float,
    toAlpha: Float,
    onAnimationEnd: (() -> Unit)? = null
) {
    clearAnimation()
    this@animateAlpha.alpha = 1F
    val anim = AlphaAnimation(formAlpha, toAlpha)
    anim.duration = duration.toLong()
    anim.fillAfter = true
    anim.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@animateAlpha.alpha = toAlpha
            onAnimationEnd?.invoke()
        }
    })
    post {
        startAnimation(anim)
    }
}

fun animCenterScale(duration: Long = 500): ScaleAnimation {
    return ScaleAnimation(
        0f, 1f, 0f, 1f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).also {
        it.duration = duration
    }
}

fun Animation?.onAnimationStart(onStart: () -> Unit): Animation? {
    this?.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationStart(animation: Animation) {
            onStart()
        }
    })
    return this
}

fun Animation?.onAnimationEnd(onEnd: () -> Unit): Animation? {
    this?.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            onEnd()
        }
    })
    return this
}

fun ObjectAnimator.onAnimatorEnd(onEnd: () -> Unit): ObjectAnimator {
    this.addListener(object : SimpleAnimatorListener {
        override fun onAnimationEnd(animator: Animator) {
            onEnd()
        }
    })
    return this
}

fun setAnim(anim: ViewPropertyAnimator, duration: Long = 300L) {
    anim.setDuration(duration).interpolator = FastOutSlowInInterpolator()
}

fun animateColor(
    @ColorInt startColor: Int,
    @ColorInt endColor: Int,
    onColorChange: (Int) -> Unit
): ValueAnimator {
    return ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor).also {
        it.duration = 800
        it.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            onColorChange(color)
        }
    }
}

interface SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation) {
    }

    override fun onAnimationEnd(animation: Animation) {
    }

    override fun onAnimationStart(animation: Animation) {
    }
}

interface SimpleAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animator: Animator) {
    }

    override fun onAnimationEnd(animator: Animator) {
    }

    override fun onAnimationCancel(animator: Animator) {
    }

    override fun onAnimationStart(animator: Animator) {
    }
}

fun Transition.beginTransition(
    layout: ConstraintLayout,
    block: ConstraintSet.() -> Unit
): Transition {
    layout.post {
        TransitionManager.beginDelayedTransition(layout, this@beginTransition)
        val set = ConstraintSet()
        set.clone(layout)
        set.block()
        set.applyTo(layout)
    }
    return this
}

fun animationFadeIn(s: Long, vararg v: View) {
    ValueAnimator.ofFloat(0f, 1f).apply {
        interpolator = LinearInterpolator()
        duration = s
        addUpdateListener { alpha ->
            val value = alpha.animatedValue as Float
            v.forEach { it.alpha = value }
        }
    }.start()
}

