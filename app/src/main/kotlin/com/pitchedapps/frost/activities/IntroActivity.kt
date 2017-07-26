package com.pitchedapps.frost.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import ca.allanwang.kau.ui.widgets.InkPageIndicator
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.setIcon
import com.github.paolorotolo.appintro.AppIntroFragment
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.intro.IntroPageAdapter
import com.pitchedapps.frost.utils.L


/**
 * Created by Allan Wang on 2017-07-25.
 */
class IntroActivity : AppCompatActivity(), ViewPager.PageTransformer {

    val viewpager: ViewPager by bindView(R.id.intro_pager)
    val indicator: InkPageIndicator by bindView(R.id.intro_indicator)
    val next: ImageButton by bindView(R.id.intro_next)

    val fragments = listOf(
            { AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(android.R.color.transparent)) },
            { AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(android.R.color.transparent)) },
            { AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(android.R.color.transparent)) },
            { AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(android.R.color.transparent)) },
            { AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(android.R.color.transparent)) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        viewpager.apply {
            setPageTransformer(false, this@IntroActivity)
            adapter = IntroPageAdapter(supportFragmentManager, fragments)
        }
        indicator.setViewPager(viewpager)
        next.setIcon(GoogleMaterial.Icon.gmd_navigate_next)
        next.setOnClickListener { viewpager.currentItem = viewpager.currentItem + 1 }

    }

    /**
     * This was a test
     * Repeatedly calling this doesn't work too well as it isn't queued
     */
    fun dragToNext() {
        if (viewpager.currentItem == fragments.size - 1) return
        var prevPosition = 0f
        ValueAnimator.ofFloat(0f, viewpager.width.toFloat()).apply {
            addUpdateListener {
                val drag = it.animatedValue as Float
                val offset = drag - prevPosition
                prevPosition = drag
                viewpager.fakeDragBy(-offset)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = viewpager.endFakeDrag()
                override fun onAnimationCancel(animation: Animator) = viewpager.endFakeDrag()
            })
            interpolator = AccelerateDecelerateInterpolator()
            duration = 500
            viewpager.beginFakeDrag()
            start()
        }
    }

    override fun transformPage(page: View, position: Float) {
        L.d("Intro position $position")
        //only apply to adjacent pages
        if ((position < 0 && position > -1) || (position > 0 && position < 1)) {
            val absPos = Math.abs(position)
            if (absPos > 0.5) page.alpha = 0f
            else page.alpha = 1f - absPos * 2
//            val alpha = sigmoidAlpha(position)
            val pageWidth = page.width
            val translateValue = position * -pageWidth
            page.translationX = (if (translateValue > -pageWidth) translateValue else 0f) * 0.8f
//            page.alpha = alpha
        } else {
            page.alpha = 1f
            page.translationX = 0f
        }
    }

    fun sigmoidAlpha(position: Float): Float = (1 / (1 + Math.exp((Math.abs(position) - 0.5) * 50))).toFloat()
}