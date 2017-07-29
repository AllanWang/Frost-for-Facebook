package com.pitchedapps.frost.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import ca.allanwang.kau.ui.views.RippleCanvas
import ca.allanwang.kau.ui.widgets.InkPageIndicator
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.statusBarColor
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.intro.*
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-07-25.
 */
class IntroActivity : AppCompatActivity(), ViewPager.PageTransformer, ViewPager.OnPageChangeListener {

    val ripple: RippleCanvas by bindView(R.id.intro_ripple)
    val viewpager: ViewPager by bindView(R.id.intro_viewpager)
    lateinit var adapter: IntroPageAdapter
    val indicator: InkPageIndicator by bindView(R.id.intro_indicator)
    val skip: Button by bindView(R.id.intro_skip)
    val done: Button by bindView(R.id.intro_done)
    val next: ImageButton by bindView(R.id.intro_next)

    val fragments = listOf(
            IntroFragmentWelcome(),
            IntroFragmentTheme(),
            IntroAccountFragment(),
            IntroTabTouchFragment(),
            IntroTabContextFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        adapter = IntroPageAdapter(supportFragmentManager, fragments)
        viewpager.apply {
            setPageTransformer(true, this@IntroActivity)
            addOnPageChangeListener(this@IntroActivity)
            adapter = this@IntroActivity.adapter
            offscreenPageLimit = 8
        }
        indicator.setViewPager(viewpager)
        next.setIcon(GoogleMaterial.Icon.gmd_navigate_next)
        next.setOnClickListener { viewpager.currentItem = viewpager.currentItem + 1 }
        ripple.set(Prefs.bgColor)
        theme()
    }

    fun theme() {
        statusBarColor = Prefs.headerColor
        navigationBarColor = Prefs.headerColor
        skip.setTextColor(Prefs.textColor)
        done.setTextColor(Prefs.textColor)
        next.imageTintList = ColorStateList.valueOf(Prefs.textColor)
        indicator.setColour(Prefs.textColor)
        indicator.invalidate()
        adapter.retainedFragments.values.forEach { it.themeFragment() }
    }

    /**
     * Transformations are mainly handled on a per view basis
     * This simplifies it by making the first fragment fade out as the second fragment comes in
     * All fragments are locked in position
     */
    override fun transformPage(page: View, position: Float) {
        //only apply to adjacent pages
        if ((position < 0 && position > -1) || (position > 0 && position < 1)) {
            val pageWidth = page.width
            val translateValue = position * -pageWidth
            page.translationX = (if (translateValue > -pageWidth) translateValue else 0f)
            page.alpha = if (position < 0) 1 + position else
//                if (position > 0.9f) (1 - position) * 10 else
                    1f
        } else {
            page.alpha = 1f
            page.translationX = 0f
        }

    }

    fun sigmoid(value: Float) = 1 / (1 + Math.exp((value - 0.5) * 10))

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        adapter[position]?.onPageScrolled(positionOffset)
        adapter[position + 1]?.onPageScrolled(positionOffset - 1)
    }

    override fun onPageSelected(position: Int) {
        adapter[position]?.onPageSelected()
    }

}