package com.pitchedapps.frost.intro

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pitchedapps.frost.utils.Prefs
import org.jetbrains.anko.childrenSequence

/**
 * Created by Allan Wang on 2017-07-28.
 */
abstract class BaseIntroFragment(val layoutRes: Int) : Fragment() {

    val screenWidth
        get() = resources.displayMetrics.widthPixels

    val Float.defaultTranslation
        get() = -Math.abs(this) * screenWidth * 0.8f

    fun translate(offset: Float, views: Array<Array<out View>>) {
        val maxTranslation = offset * screenWidth
        val increment = maxTranslation / views.size
        views.forEachIndexed { i, group ->
            group.forEach {
                it.translationX = if (offset > 0) -maxTranslation + i * increment else -(i + 1) * increment
                it.alpha = 1 - Math.abs(offset)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutRes, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeFragment()
    }

    open fun themeFragment() {
        view?.childrenSequence()?.forEach { (it as? TextView)?.setTextColor(Prefs.textColor) }
    }

    fun View.scale(position: Float, offset: Double) {
        val scale = (0.3 / (1 + Math.exp(10.0 * (Math.abs(position) - 1 + offset))) + 0.7).toFloat()
        scaleX = scale
        scaleY = scale
    }

    val viewArray: Array<Array<out View>> by lazy { viewArray() }

    abstract fun viewArray(): Array<Array<out View>>

    open fun onPageScrolled(positionOffset: Float) {
        translate(positionOffset, viewArray)
    }

    open fun onPageSelected() {

    }

}