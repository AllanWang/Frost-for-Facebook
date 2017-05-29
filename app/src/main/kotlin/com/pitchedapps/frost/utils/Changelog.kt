package com.pitchedapps.frost.utils

import android.content.Context
import android.content.res.XmlResourceParser
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.support.annotation.XmlRes
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.pitchedapps.frost.R
import org.xmlpull.v1.XmlPullParser
import java.util.*


/**
 * Created by Allan Wang on 2017-05-28.
 */
class Changelog {
    companion object {
        fun show(@NonNull activity: FragmentActivity, @XmlRes xmlRes: Int = R.xml.changelog) {
            val mHandler = Handler()
            Thread(Runnable {
                val items = parse(activity, xmlRes)
                mHandler.post(object : TimerTask() {
                    override fun run() {
                        MaterialDialog.Builder(activity)
                                .title(R.string.changelog)
                                .positiveText(R.string.great)
                                .adapter(ChangelogAdapter(items), null)
                                .show()
                    }
                })
            }).start()
        }
    }
}

private class ChangelogAdapter(val items: List<Pair<String, ChangelogType>>) : RecyclerView.Adapter<ChangelogAdapter.ChangelogVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChangelogVH(LayoutInflater.from(parent.context)
            .inflate(getLayout(viewType), parent, false))

    private fun getLayout(position: Int) = items[position].second.layout

    override fun onBindViewHolder(holder: ChangelogVH, position: Int) {
        holder.text.text = items[position].first
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    override fun getItemCount() = items.size

    internal class ChangelogVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.changelog_text) as TextView
    }
}

private fun parse(context: Context, @XmlRes xmlRes: Int): List<Pair<String, ChangelogType>> {
    val items = mutableListOf<Pair<String, ChangelogType>>()
    context.resources.getXml(xmlRes).use {
        parser ->
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG)
                ChangelogType.values.any { it.add(parser, items) }
            eventType = parser.next()
        }
    }
    return items
}

private enum class ChangelogType(val tag: String, val attr: String, @LayoutRes val layout: Int) {
    TITLE("title", "version", R.layout.changelog_title),
    ITEM("item", "text", R.layout.changelog_content);

    companion object {
        val values = values()
    }

    /**
     * Returns true if tag matches; false otherwise
     */
    fun add(parser: XmlResourceParser, list: MutableList<Pair<String, ChangelogType>>): Boolean {
        if (parser.name != tag) return false
        if (parser.getAttributeValue(null, attr).isNotBlank())
            list.add(Pair(parser.getAttributeValue(null, attr), this))
        return true
    }
}

