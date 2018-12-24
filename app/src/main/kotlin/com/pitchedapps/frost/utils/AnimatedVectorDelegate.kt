package com.pitchedapps.frost.utils

import android.graphics.drawable.AnimatedVectorDrawable
import androidx.annotation.DrawableRes
import android.widget.ImageView
import ca.allanwang.kau.utils.drawable

/**
 * Created by Allan Wang on 2017-07-29.
 *
 * Delegate for animated vector drawables with two states (start and end)
 * Drawables are added lazily depending on the animation direction, and are verified upon load
 * Should the bounded view not have an animated drawable upon animating, it is assumed
 * that the user has switched the resource themselves and the delegate will not switch the resource
 */
interface AnimatedVectorContract {
    fun animate()
    fun animateReverse()
    fun animateToggle()
    val isAtStart: Boolean
    fun bind(view: ImageView)
    var animatedVectorListener: ((avd: AnimatedVectorDrawable, forwards: Boolean) -> Unit)?
}

class AnimatedVectorDelegate(
        /**
         * The res for the starting resource; must have parent tag animated-vector
         */
        @param:DrawableRes val avdStart: Int,
        /**
         * The res for the ending resource; must have parent tag animated-vector
         */
        @param:DrawableRes val avdEnd: Int,
        /**
         * The delegate will automatically set the start resource when bound
         * If [emitOnBind] is true, it will also trigger the listener
         */
        val emitOnBind: Boolean = true,
        /**
         * The optional listener that will be triggered every time the avd is switched by the delegate
         */
        override var animatedVectorListener: ((avd: AnimatedVectorDrawable, forwards: Boolean) -> Unit)? = null
) : AnimatedVectorContract {

    lateinit var view: ImageView

    private var atStart = true

    override val isAtStart: Boolean
        get() = atStart

    private val avd: AnimatedVectorDrawable?
        get() = view.drawable as? AnimatedVectorDrawable

    override fun bind(view: ImageView) {
        this.view = view
        view.context.drawable(avdStart) as? AnimatedVectorDrawable
                ?: throw IllegalArgumentException("AnimatedVectorDelegate has a starting drawable that isn't an avd")
        view.context.drawable(avdEnd) as? AnimatedVectorDrawable
                ?: throw IllegalArgumentException("AnimatedVectorDelegate has an ending drawable that isn't an avd")
        view.setImageResource(avdStart)
        if (emitOnBind) animatedVectorListener?.invoke(avd!!, false)
    }

    override fun animate() = animateImpl(false)

    override fun animateReverse() = animateImpl(true)

    override fun animateToggle() = animateImpl(!atStart)

    private fun animateImpl(toStart: Boolean) {
        if ((atStart == toStart)) return L.d { "AVD already at ${if (toStart) "start" else "end"}" }
        if (avd == null) return L.d { "AVD null resource" }//no longer using animated vector; do not modify
        avd?.stop()
        view.setImageResource(if (toStart) avdEnd else avdStart)
        animatedVectorListener?.invoke(avd!!, !toStart)
        atStart = toStart
        avd?.start()
    }

}



