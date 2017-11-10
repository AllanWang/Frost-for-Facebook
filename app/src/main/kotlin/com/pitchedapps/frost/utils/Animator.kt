package com.pitchedapps.frost.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.Interpolator

/**
 * Created by Allan Wang on 2017-11-10.
 */
class ProgressAnimator private constructor(private vararg val values: Float) {

    companion object {
        inline fun ofFloat(crossinline builder: ProgressAnimator.() -> Unit) = ofFloat(0f, 1f) { builder() }

        fun ofFloat(vararg values: Float, builder: ProgressAnimator.() -> Unit) = ProgressAnimator(*values).apply {
            builder()
            build()
        }
    }

    private val animators: MutableList<(Float) -> Unit> = mutableListOf()
    private val startActions: MutableList<() -> Unit> = mutableListOf()
    private val endActions: MutableList<() -> Unit> = mutableListOf()

    var duration: Long = -1L
    var interpolator: Interpolator? = null

    /**
     * Add more changes to the [ValueAnimator] before running
     */
    var extraConfigs: ValueAnimator.() -> Unit = {}

    fun withAnimator(from: Float, to: Float, animator: (Float) -> Unit) = animators.add {
        val range = to - from
        animator(range * it + from)
    }

    fun withAnimator(animator: (Float) -> Unit) = animators.add(animator)

    fun withAnimatorInv(animator: (Float) -> Unit) = animators.add { animator(1f - it) }

    fun withStartAction(action: () -> Unit) = startActions.add(action)

    fun withEndAction(action: () -> Unit) = endActions.add(action)

    fun build() {
        ValueAnimator.ofFloat(*values).apply {
            if (this@ProgressAnimator.duration > 0L)
                duration = this@ProgressAnimator.duration
            if (this@ProgressAnimator.interpolator != null)
                interpolator = this@ProgressAnimator.interpolator
            addUpdateListener {
                val progress = it.animatedValue as Float
                animators.forEach { it(progress) }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    startActions.forEach { it() }
                }

                override fun onAnimationEnd(animation: Animator?) {
                    endActions.forEach { it() }
                }
            })
            extraConfigs()
            start()
        }
    }
}