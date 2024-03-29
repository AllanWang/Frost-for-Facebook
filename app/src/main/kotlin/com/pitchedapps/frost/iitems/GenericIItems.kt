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
package com.pitchedapps.frost.iitems

import android.content.Context
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.ui.createSimpleRippleDrawable
import ca.allanwang.kau.utils.bindView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.select.selectExtension
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.launchWebOverlay

/** Created by Allan Wang on 30/12/17. */

/** Base contract for anything with a url that may be launched in a new overlay */
interface ClickableIItemContract {

  val url: String?

  fun click(context: Context, fbCookie: FbCookie, prefs: Prefs) {
    val url = url ?: return
    context.launchWebOverlay(url, fbCookie, prefs)
  }

  companion object {
    fun bindEvents(adapter: IAdapter<GenericItem>, fbCookie: FbCookie, prefs: Prefs) {
      adapter.fastAdapter?.apply {
        selectExtension { isSelectable = false }
        onClickListener = { v, _, item, _ ->
          if (item is ClickableIItemContract) {
            item.click(v!!.context, fbCookie, prefs)
            true
          } else false
        }
      }
    }
  }
}

/** Generic header item Not clickable with an accent color */
open class HeaderIItem(
  val text: String?,
  itemId: Int = R.layout.iitem_header,
  private val themeProvider: ThemeProvider
) :
  KauIItem<HeaderIItem.ViewHolder>(
    R.layout.iitem_header,
    { ViewHolder(it, themeProvider) },
    itemId
  ) {

  class ViewHolder(itemView: View, private val themeProvider: ThemeProvider) :
    FastAdapter.ViewHolder<HeaderIItem>(itemView) {

    val text: TextView by bindView(R.id.item_header_text)

    override fun bindView(item: HeaderIItem, payloads: List<Any>) {
      text.setTextColor(themeProvider.accentColor)
      text.text = item.text
      text.setBackgroundColor(themeProvider.nativeBgColor)
    }

    override fun unbindView(item: HeaderIItem) {
      text.text = null
    }
  }
}

/** Generic text item Clickable with text color */
open class TextIItem(
  val text: String?,
  override val url: String?,
  itemId: Int = R.layout.iitem_text,
  private val themeProvider: ThemeProvider
) :
  KauIItem<TextIItem.ViewHolder>(R.layout.iitem_text, { ViewHolder(it, themeProvider) }, itemId),
  ClickableIItemContract {

  class ViewHolder(itemView: View, private val themeProvider: ThemeProvider) :
    FastAdapter.ViewHolder<TextIItem>(itemView) {

    val text: TextView by bindView(R.id.item_text_view)

    override fun bindView(item: TextIItem, payloads: List<Any>) {
      text.setTextColor(themeProvider.textColor)
      text.text = item.text
      text.background =
        createSimpleRippleDrawable(themeProvider.bgColor, themeProvider.nativeBgColor)
    }

    override fun unbindView(item: TextIItem) {
      text.text = null
    }
  }
}
