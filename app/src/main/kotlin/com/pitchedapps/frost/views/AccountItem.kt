package com.pitchedapps.frost.views

import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-05.
 */
class AccountItem(val cookie: CookieModel?) : AbstractItem<AccountItem, AccountItem.ViewHolder>() {

    override fun getType(): Int = R.id.item_account

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.view_account

    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)
        with(viewHolder) {
            text.visibility = View.INVISIBLE
            text.setTextColor(Prefs.textColor)
            if (cookie != null) {
                text.text = cookie.name
                val options = RequestOptions().transform(CircleCrop())
                Glide.with(itemView).load(PROFILE_PICTURE_URL(cookie.id)).apply(options).listener(object : RequestListener<Drawable> {
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
                text.visibility = View.VISIBLE
                image.setImageDrawable(GoogleMaterial.Icon.gmd_add_circle_outline.toDrawable(itemView.context, 100, Prefs.textColor))
                text.text = itemView.context.getString(R.string.add_account)
                //todo add plus image
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