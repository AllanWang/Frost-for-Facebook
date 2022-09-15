/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.activities

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.kotlin.lazyUi
import ca.allanwang.kau.utils.blendWith
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.fadeScaleTransition
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.scaleXY
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.statusBarColor
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.databinding.ActivityIntroBinding
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.intro.BaseIntroFragment
import com.pitchedapps.frost.intro.IntroAccountFragment
import com.pitchedapps.frost.intro.IntroFragmentEnd
import com.pitchedapps.frost.intro.IntroFragmentTheme
import com.pitchedapps.frost.intro.IntroFragmentWelcome
import com.pitchedapps.frost.intro.IntroTabContextFragment
import com.pitchedapps.frost.intro.IntroTabTouchFragment
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.ActivityThemer
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.loadAssets
import com.pitchedapps.frost.widgets.NotificationWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 2017-07-25.
 *
 * A beautiful intro activity Phone showcases are drawn via layers
 */
@AndroidEntryPoint
class IntroActivity : KauBaseActivity(), ViewPager.PageTransformer, ViewPager.OnPageChangeListener {

  @Inject lateinit var prefs: Prefs

  @Inject lateinit var themeProvider: ThemeProvider

  @Inject lateinit var activityThemer: ActivityThemer

  lateinit var binding: ActivityIntroBinding
  private var barHasNext = true

  private val fragments by lazyUi {
    listOf(
      IntroFragmentWelcome(),
      IntroFragmentTheme(),
      IntroAccountFragment(),
      IntroTabTouchFragment(),
      IntroTabContextFragment(),
      IntroFragmentEnd()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityIntroBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.init()
  }

  private fun ActivityIntroBinding.init() {
    viewpager.apply {
      setPageTransformer(true, this@IntroActivity)
      addOnPageChangeListener(this@IntroActivity)
      adapter = IntroPageAdapter(supportFragmentManager, fragments)
    }
    indicator.setViewPager(viewpager)
    next.setIcon(GoogleMaterial.Icon.gmd_navigate_next)
    next.setOnClickListener {
      if (barHasNext) viewpager.setCurrentItem(viewpager.currentItem + 1, true)
      else finish(next.x + next.pivotX, next.y + next.pivotY)
    }
    skip.setOnClickListener { finish() }
    ripple.set(themeProvider.bgColor)
    theme()
  }

  fun theme() {
    statusBarColor = themeProvider.headerColor
    navigationBarColor = themeProvider.headerColor
    with(binding) {
      skip.setTextColor(themeProvider.textColor)
      next.imageTintList = ColorStateList.valueOf(themeProvider.textColor)
      indicator.setColour(themeProvider.textColor)
      indicator.invalidate()
    }
    fragments.forEach { it.themeFragment() }
    activityThemer.setFrostTheme(forceTransparent = true)
  }

  /**
   * Transformations are mainly handled on a per view basis This makes the first fragment fade out
   * as the second fragment comes in All fragments are locked in position
   */
  override fun transformPage(page: View, position: Float) {
    // only apply to adjacent pages
    if ((position < 0 && position > -1) || (position > 0 && position < 1)) {
      val pageWidth = page.width
      val translateValue = position * -pageWidth
      page.translationX = (if (translateValue > -pageWidth) translateValue else 0f)
      page.alpha = if (position < 0) 1 + position else 1f
    } else {
      page.alpha = 1f
      page.translationX = 0f
    }
  }

  fun finish(x: Float, y: Float) {
    val blue = color(R.color.facebook_blue)
    window.setFlags(
      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
    binding.ripple.ripple(blue, x, y, 600) { postDelayed(1000) { finish() } }
    val lastView: View? = fragments.last().view
    arrayOf<View?>(
        binding.skip,
        binding.indicator,
        binding.next,
        lastView?.findViewById(R.id.intro_title),
        lastView?.findViewById(R.id.intro_desc)
      )
      .forEach { it?.animate()?.alpha(0f)?.setDuration(600)?.start() }
    if (themeProvider.textColor != Color.WHITE) {
      val f = lastView?.findViewById<ImageView>(R.id.intro_image)?.drawable
      if (f != null)
        ValueAnimator.ofFloat(0f, 1f).apply {
          addUpdateListener {
            f.setTint(themeProvider.textColor.blendWith(Color.WHITE, it.animatedValue as Float))
          }
          duration = 600
          start()
        }
    }
    if (themeProvider.headerColor != blue) {
      ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener {
          val c = themeProvider.headerColor.blendWith(blue, it.animatedValue as Float)
          statusBarColor = c
          navigationBarColor = c
        }
        duration = 600
        start()
      }
    }
  }

  override fun finish() {
    launch(NonCancellable) {
      loadAssets(themeProvider)
      NotificationWidget.forceUpdate(this@IntroActivity)
      launchNewTask<MainActivity>(cookies(), false)
      super.finish()
    }
  }

  override fun onBackPressed() {
    with(binding) {
      if (viewpager.currentItem > 0) viewpager.setCurrentItem(viewpager.currentItem - 1, true)
      else finish()
    }
  }

  override fun onPageScrollStateChanged(state: Int) {}

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    fragments[position].onPageScrolled(positionOffset)
    if (position + 1 < fragments.size) fragments[position + 1].onPageScrolled(positionOffset - 1)
  }

  override fun onPageSelected(position: Int) {
    fragments[position].onPageSelected()
    val hasNext = position != fragments.size - 1
    if (barHasNext == hasNext) return
    barHasNext = hasNext
    binding.next.fadeScaleTransition {
      setIcon(
        if (barHasNext) GoogleMaterial.Icon.gmd_navigate_next else GoogleMaterial.Icon.gmd_done,
        color = themeProvider.textColor
      )
    }
    binding.skip.animate().scaleXY(if (barHasNext) 1f else 0f)
  }

  class IntroPageAdapter(fm: FragmentManager, private val fragments: List<BaseIntroFragment>) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size
  }
}
