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

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toDrawable
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.prefs.Prefs
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Allan Wang on 2017-06-19.
 */
class Keywords @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val prefs: Prefs by inject()
    val editText: AppCompatEditText by bindView(R.id.edit_text)
    val addIcon: ImageView by bindView(R.id.add_icon)
    val recycler: RecyclerView by bindView(R.id.recycler)
    val adapter = FastItemAdapter<KeywordItem>()

    init {
        inflate(context, R.layout.view_keywords, this)
        editText.tint(prefs.textColor)
        addIcon.setImageDrawable(GoogleMaterial.Icon.gmd_add.keywordDrawable(context, prefs))
        addIcon.setOnClickListener {
            if (editText.text.isNullOrEmpty()) editText.error =
                context.string(R.string.empty_keyword)
            else {
                adapter.add(0, KeywordItem(editText.text.toString()))
                editText.text?.clear()
            }
        }
        adapter.add(prefs.notificationKeywords.map { KeywordItem(it) })
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter.addEventHook(object : ClickEventHook<KeywordItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? =
                (viewHolder as? KeywordItem.ViewHolder)?.delete

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<KeywordItem>,
                item: KeywordItem
            ) {
                adapter.remove(position)
            }
        })
    }

    fun save() {
        prefs.notificationKeywords = adapter.adapterItems.mapTo(mutableSetOf()) { it.keyword }
    }
}

private fun IIcon.keywordDrawable(context: Context, prefs: Prefs): Drawable =
    toDrawable(context, 20, prefs.textColor)

class KeywordItem(val keyword: String) : AbstractItem<KeywordItem.ViewHolder>() {

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override val layoutRes: Int
        get() = R.layout.item_keyword

    override val type: Int
        get() = R.id.item_keyword

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        holder.text.text = keyword
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.text.text = null
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v), KoinComponent {

        private val prefs: Prefs by inject()
        val text: AppCompatTextView by bindView(R.id.keyword_text)
        val delete: ImageView by bindView(R.id.keyword_delete)

        init {
            text.setTextColor(prefs.textColor)
            delete.setImageDrawable(
                GoogleMaterial.Icon.gmd_delete.keywordDrawable(
                    itemView.context,
                    prefs
                )
            )
        }
    }
}
