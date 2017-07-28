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

    var bounded = false

    val screenWidth
        get() = resources.displayMetrics.widthPixels

    val Float.defaultTranslation
        get() = -Math.abs(this) * screenWidth * 0.4f

    fun translate(offset: Float, views: Array<Array<out View>>) {
        val maxTranslation = Math.abs(offset) * screenWidth * 0.3f
        val increment = 2 * maxTranslation / views.size
        views.forEachIndexed { i, group -> group.forEach { it.translationX = -maxTranslation + i * increment } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutRes, container, false)
        bounded = true
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeFragment()
    }

    open fun themeFragment() {
        view?.childrenSequence()?.forEach {
            (it as? TextView)?.setTextColor(Prefs.textColor)
//            (it as? Button)?.setRippleBackground(Prefs.accentColor, Color.TRANSPARENT)
        }
    }

    fun View.scale(position: Float, offset: Double) {
        val scale = (0.3 / (1 + Math.exp(10.0 * (Math.abs(position) - 1 + offset))) + 0.7).toFloat()
        scaleX = scale
        scaleY = scale
    }

    override fun onStart() {
        super.onStart()
        bounded = true
    }

    override fun onStop() {
        bounded = false
        super.onStop()
    }

    val viewArray: Array<Array<out View>> by lazy { viewArray() }

    abstract fun viewArray(): Array<Array<out View>>

    open fun onPageScrolled(positionOffset: Float) {
        translate(positionOffset, viewArray)
    }

    open fun onPageSelected() {

    }

}