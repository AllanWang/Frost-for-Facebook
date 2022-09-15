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
package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefColorPicker
import ca.allanwang.kau.kpref.activity.items.KPrefSeekbar
import ca.allanwang.kau.ui.views.RippleCanvas
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.string
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.enums.MainActivityLayout
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.utils.REQUEST_NAV
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.frostNavigationBar
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.launchTabCustomizerActivity
import com.pitchedapps.frost.views.KPrefTextSeekbar

/** Created by Allan Wang on 2017-06-29. */
fun SettingsActivity.getAppearancePrefs(): KPrefAdapterBuilder.() -> Unit = {
  header(R.string.theme_customization)

  text(R.string.theme, prefs::theme, { themeProvider.setTheme(it) }) {
    onClick = {
      materialDialog {
        title(R.string.theme)
        listItemsSingleChoice(
          items = Theme.values().map { string(it.textRes) },
          initialSelection = item.pref
        ) { _, index, _ ->
          if (item.pref != index) {
            item.pref = index
            shouldRestartMain()
            reload()
            activityThemer.setFrostTheme(forceTransparent = true)
            themeExterior()
            invalidateOptionsMenu()
            frostEvent("Theme", "Count" to Theme(index).name)
          }
        }
      }
    }
    textGetter = { string(Theme(it).textRes) }
  }

  fun KPrefColorPicker.KPrefColorContract.dependsOnCustom() {
    enabler = themeProvider::isCustomTheme
    onDisabledClick = { frostSnackbar(R.string.requires_custom_theme, themeProvider) }
    allowCustom = true
  }

  fun invalidateCustomTheme() {
    themeProvider.reset()
  }

  colorPicker(
    R.string.text_color,
    prefs::customTextColor,
    {
      prefs.customTextColor = it
      reload()
      invalidateCustomTheme()
      shouldRestartMain()
    }
  ) {
    dependsOnCustom()
    allowCustomAlpha = false
  }

  colorPicker(
    R.string.accent_color,
    prefs::customAccentColor,
    {
      prefs.customAccentColor = it
      reload()
      invalidateCustomTheme()
      shouldRestartMain()
    }
  ) {
    dependsOnCustom()
    allowCustomAlpha = false
  }

  colorPicker(
    R.string.background_color,
    prefs::customBackgroundColor,
    {
      prefs.customBackgroundColor = it
      bgCanvas.ripple(it, duration = 500L)
      invalidateCustomTheme()
      activityThemer.setFrostTheme(forceTransparent = true)
      shouldRestartMain()
    }
  ) {
    dependsOnCustom()
    allowCustomAlpha = true
  }

  colorPicker(
    R.string.header_color,
    prefs::customHeaderColor,
    {
      prefs.customHeaderColor = it
      frostNavigationBar(prefs, themeProvider)
      toolbarCanvas.ripple(it, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
      reload()
      shouldRestartMain()
    }
  ) {
    dependsOnCustom()
    allowCustomAlpha = true
  }

  colorPicker(
    R.string.icon_color,
    prefs::customIconColor,
    {
      prefs.customIconColor = it
      invalidateOptionsMenu()
      shouldRestartMain()
    }
  ) {
    dependsOnCustom()
    allowCustomAlpha = false
  }

  header(R.string.global_customization)

  text(
    R.string.main_activity_layout,
    prefs::mainActivityLayoutType,
    { prefs.mainActivityLayoutType = it }
  ) {
    textGetter = { string(prefs.mainActivityLayout.titleRes) }
    onClick = {
      materialDialog {
        title(R.string.main_activity_layout_desc)
        listItemsSingleChoice(
          items = MainActivityLayout.values.map { string(it.titleRes) },
          initialSelection = item.pref
        ) { _, index, _ ->
          if (item.pref != index) {
            item.pref = index
            shouldRestartMain()
            frostEvent("Main Layout", "Type" to MainActivityLayout(index).name)
          }
        }
      }
    }
  }

  plainText(R.string.main_tabs) {
    descRes = R.string.main_tabs_desc
    onClick = { launchTabCustomizerActivity() }
  }

  checkbox(
    R.string.tint_nav,
    prefs::tintNavBar,
    {
      prefs.tintNavBar = it
      frostNavigationBar(prefs, themeProvider)
      setFrostResult(REQUEST_NAV)
    }
  ) {
    descRes = R.string.tint_nav_desc
  }

  list.add(
    KPrefTextSeekbar(
      KPrefSeekbar.KPrefSeekbarBuilder(
        globalOptions,
        R.string.web_text_scaling,
        prefs::webTextScaling
      ) {
        prefs.webTextScaling = it
        setFrostResult(REQUEST_TEXT_ZOOM)
      }
    )
  )

  checkbox(R.string.enforce_black_media_bg, prefs::blackMediaBg, { prefs.blackMediaBg = it }) {
    descRes = R.string.enforce_black_media_bg_desc
  }
}
