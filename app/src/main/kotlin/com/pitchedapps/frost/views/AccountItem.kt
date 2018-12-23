package com.pitchedapps.frost.views

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.utils.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-05.
 */
class AccountItem(val cookie: CookieModel?) : KauIItem<AccountItem, AccountItem.ViewHolder>
(R.layout.view_account, { ViewHolder(it) }, R.id.item_account) {

    override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(viewHolder, payloads)
        with(viewHolder) {
            text.invisible()
            text.setTextColor(Prefs.textColor)
            if (cookie != null) {
                text.text = cookie.name
                GlideApp.with(itemView).load(profilePictureUrl(cookie.id))
                        .transform(FrostGlide.roundCorner).listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                text.fadeIn()
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                text.fadeIn()
                                return false
                            }
                        }).into(image)
            } else {
                text.visible()
                image.setImageDrawable(GoogleMaterial.Icon.gmd_add_circle_outline.toDrawable(itemView.context, 100, Prefs.textColor))
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

    class ViewHolder(val v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val image: ImageView by bindView(R.id.account_image)
        val text: AppCompatTextView by bindView(R.id.account_text)
    }
}