package com.pitchedapps.frost.views

import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mikepenz.fastadapter.items.AbstractItem
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.bindView

/**
 * Created by Allan Wang on 2017-06-05.
 */
class AccountItem(val id: Long, val name: String) : AbstractItem<AccountItem, AccountItem.ViewHolder>() {
    constructor() : this(-1L, "")
    constructor(cookie: CookieModel) : this(cookie.id, cookie.name ?: "")

    override fun getType(): Int = R.id.item_account

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.view_account

    override fun bindView(viewHolder: ViewHolder, payloads: List<Any>) {
        super.bindView(viewHolder, payloads)
        with(viewHolder) {
            text.visibility = View.INVISIBLE
            if (id != -1L) {
                text.text = name
                Glide.with(itemView).load(PROFILE_PICTURE_URL(id)).listener(object : RequestListener<Drawable> {
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

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView by bindView(R.id.account_image)
        val text: AppCompatTextView by bindView(R.id.account_text)

        init {
            ButterKnife.bind(v)
        }
    }
}