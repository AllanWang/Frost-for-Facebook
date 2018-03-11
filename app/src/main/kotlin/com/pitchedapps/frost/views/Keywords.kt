package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.kpref.StringSet
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toDrawable
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-06-19.
 */
class Keywords @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val editText: AppCompatEditText by bindView(R.id.edit_text)
    val addIcon: ImageView by bindView(R.id.add_icon)
    val recycler: RecyclerView by bindView(R.id.recycler)
    val adapter = FastItemAdapter<KeywordItem>()

    init {
        inflate(context, R.layout.view_keywords, this)
        editText.tint(Prefs.textColor)
        addIcon.setImageDrawable(GoogleMaterial.Icon.gmd_add.keywordDrawable(context))
        addIcon.setOnClickListener {
            if (editText.text.isEmpty()) editText.error = context.string(R.string.empty_keyword)
            else {
                adapter.add(0, KeywordItem(editText.text.toString()))
                editText.text.clear()
            }
        }
        adapter.add(Prefs.notificationKeywords.map { KeywordItem(it) })
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter.withEventHook(object : ClickEventHook<KeywordItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? = (viewHolder as? KeywordItem.ViewHolder)?.delete

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<KeywordItem>, item: KeywordItem) {
                adapter.remove(position)
            }
        })
    }

    fun save() {
        val keywords = adapter.adapterItems.map { it.keyword }
        Prefs.notificationKeywords = StringSet(keywords)
    }


}

private fun IIcon.keywordDrawable(context: Context): Drawable = toDrawable(context, 20, Prefs.textColor)

class KeywordItem(val keyword: String) : AbstractItem<KeywordItem, KeywordItem.ViewHolder>() {

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getType(): Int = R.id.item_keyword

    override fun getLayoutRes(): Int = R.layout.item_keyword

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        holder.text.text = keyword
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.text.text = null
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val text: AppCompatTextView by bindView(R.id.keyword_text)
        val delete: ImageView by bindView(R.id.keyword_delete)

        init {
            text.setTextColor(Prefs.textColor)
            delete.setImageDrawable(GoogleMaterial.Icon.gmd_delete.keywordDrawable(itemView.context))
        }
    }
}