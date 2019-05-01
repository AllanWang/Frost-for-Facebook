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
package com.pitchedapps.frost.views

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.invisible
import ca.allanwang.kau.utils.toDrawable
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-05.
 */
class AccountItem(val cookie: CookieEntity?) : KauIItem<AccountItem, AccountItem.ViewHolder>
    (R.layout.view_account, { ViewHolder(it) }, R.id.item_account) {

    override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(viewHolder, payloads)
        with(viewHolder) {
            text.invisible()
            text.setTextColor(Prefs.textColor)
            if (cookie != null) {
                text.text = cookie.name
                GlideApp.with(itemView).load(profilePictureUrl(cookie.id))
                    .transform(FrostGlide.circleCrop).listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            text.fadeIn()
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            text.fadeIn()
                            return false
                        }
                    }).into(image)
            } else {
                text.visible()
                image.setImageDrawable(
                    GoogleMaterial.Icon.gmd_add_circle_outline.toDrawable(
                        itemView.context,
                        100,
                        Prefs.textColor
                    )
                )
                text.text = itemView.context.getString(R.string.kau_add_account)
            }
        }
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        with(holder) {
            text.text = null
            image.setImageDrawable(null)
        }
    }

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView by bindView(R.id.account_image)
        val text: AppCompatTextView by bindView(R.id.account_text)
    }
}
