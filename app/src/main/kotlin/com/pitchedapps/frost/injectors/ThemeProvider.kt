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
package com.pitchedapps.frost.injectors

import android.content.Context
import android.graphics.Color
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToBackground
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.toRgbaString
import ca.allanwang.kau.utils.use
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.enums.FACEBOOK_BLUE
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.enums.ThemeCategory
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.BufferedReader
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ThemeProvider {
  val textColor: Int

  val accentColor: Int

  val accentColorForWhite: Int

  val nativeBgColor: Int

  fun nativeBgColor(unread: Boolean): Int

  val bgColor: Int

  val headerColor: Int

  val iconColor: Int

  val isCustomTheme: Boolean

  /** Note that while this can be loaded from any thread, it is typically done through [preload]] */
  fun injector(category: ThemeCategory): InjectorContract

  fun setTheme(id: Int)

  fun reset()

  suspend fun preload()
}

/**
 * Provides [InjectorContract] for each [ThemeCategory]. Can be reloaded to take in changes from
 * [Prefs]
 */
class ThemeProviderImpl
@Inject
internal constructor(@ApplicationContext private val context: Context, private val prefs: Prefs) :
  ThemeProvider {

  private var theme: Theme = Theme.values[prefs.theme]
    set(value) {
      field = value
      prefs.theme = value.ordinal
    }

  private val injectors: MutableMap<ThemeCategory, InjectorContract> = mutableMapOf()

  override val textColor: Int
    get() = theme.textColorGetter(prefs)

  override val accentColor: Int
    get() = theme.accentColorGetter(prefs)

  override val accentColorForWhite: Int
    get() =
      when {
        accentColor.isColorVisibleOn(Color.WHITE) -> accentColor
        textColor.isColorVisibleOn(Color.WHITE) -> textColor
        else -> FACEBOOK_BLUE
      }

  override val nativeBgColor: Int
    get() = bgColor.withAlpha(30)

  override fun nativeBgColor(unread: Boolean) =
    bgColor.colorToForeground(if (unread) 0.7f else 0.0f).withAlpha(30)

  override val bgColor: Int
    get() = theme.backgroundColorGetter(prefs)

  override val headerColor: Int
    get() = theme.headerColorGetter(prefs)

  override val iconColor: Int
    get() = theme.iconColorGetter(prefs)

  override val isCustomTheme: Boolean
    get() = theme == Theme.CUSTOM

  override fun injector(category: ThemeCategory): InjectorContract =
    injectors.getOrPut(category) { createInjector(category) }

  /** Note that while this can be loaded from any thread, it is typically done through [preload] */
  private fun createInjector(category: ThemeCategory): InjectorContract {
    val file = theme.file ?: return JsActions.EMPTY
    try {
      var content =
        context.assets
          .open("css/${category.folder}/themes/$file")
          .bufferedReader()
          .use(BufferedReader::readText)
      if (theme == Theme.CUSTOM) {
        val bt = if (Color.alpha(bgColor) == 255) bgColor.toRgbaString() else "transparent"

        val bb = bgColor.colorToForeground(0.35f)

        content =
          content
            .replace("\$T\$", textColor.toRgbaString())
            .replace("\$TT\$", textColor.colorToBackground(0.05f).toRgbaString())
            .replace("\$TD\$", textColor.adjustAlpha(0.6f).toRgbaString())
            .replace("\$A\$", accentColor.toRgbaString())
            .replace("\$AT\$", iconColor.toRgbaString())
            .replace("\$B\$", bgColor.toRgbaString())
            .replace("\$BT\$", bt)
            .replace("\$BBT\$", bb.withAlpha(51).toRgbaString())
            .replace("\$O\$", bgColor.withAlpha(255).toRgbaString())
            .replace("\$OO\$", bb.withAlpha(255).toRgbaString())
            .replace("\$D\$", textColor.adjustAlpha(0.3f).toRgbaString())
            .replace("\$TI\$", bb.withAlpha(60).toRgbaString())
            .replace("\$C\$", bt)
      }
      return JsBuilder().css(content).build()
    } catch (e: FileNotFoundException) {
      L.e(e) { "CssAssets file not found" }
      return JsActions.EMPTY
    }
  }

  override fun setTheme(id: Int) {
    if (theme.ordinal == id) return
    theme = Theme.values[id]
    reset()
  }

  override fun reset() {
    injectors.clear()
  }

  override suspend fun preload() {
    withContext(Dispatchers.IO) {
      reset()
      ThemeCategory.values().forEach { injector(it) }
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
interface ThemeProviderModule {
  @Binds @Singleton fun themeProvider(to: ThemeProviderImpl): ThemeProvider
}
