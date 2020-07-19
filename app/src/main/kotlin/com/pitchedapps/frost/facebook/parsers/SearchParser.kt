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
package com.pitchedapps.frost.facebook.parsers

import ca.allanwang.kau.searchview.SearchItem
import com.pitchedapps.frost.facebook.FACEBOOK_BASE_COM
import com.pitchedapps.frost.facebook.FACEBOOK_MBASIC_COM
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.parsers.FrostSearch.Companion.create
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.urlEncode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-10-09.
 */
object SearchParser : FrostParser<FrostSearches> by SearchParserImpl() {
    fun query(cookie: String?, input: String): ParseResponse<FrostSearches>? {
        val url =
            "${FbItem._SEARCH_PARSE.url}/?q=${if (input.isNotBlank()) input.urlEncode() else "a"}"
        L._i { "Search Query $url" }
        return parseFromUrl(cookie, url)
    }
}

enum class SearchKeys(val key: String) {
    USERS("keywords_users"),
    EVENTS("keywords_events")
}

data class FrostSearches(val results: List<FrostSearch>) : ParseData {

    override val isEmpty: Boolean
        get() = results.isEmpty()

    override fun toString() = StringBuilder().apply {
        append("FrostSearches {\n")
        append(results.toJsonString("results", 1))
        append("}")
    }.toString()
}

/**
 * As far as I'm aware, all links are independent, so the queries don't matter
 * A lot of it is tracking information, which I'll strip away
 * Other text items are formatted for safety
 *
 * Note that it's best to create search results from [create]
 */
data class FrostSearch(val href: String, val title: String, val description: String?) {

    fun toSearchItem() = SearchItem(href, title, description)

    companion object {
        fun create(href: String, title: String, description: String?) = FrostSearch(
            with(href.indexOf("?")) { if (this == -1) href else href.substring(0, this) },
            title.format(),
            description?.format()
        )
    }
}

private class SearchParserImpl : FrostParserBase<FrostSearches>(false) {

    override var nameRes = FbItem._SEARCH_PARSE.titleId

    override val url = "${FbItem._SEARCH_PARSE.url}?q=google"

    private val String.formattedSearchUrl: String
        get() = replace(FACEBOOK_MBASIC_COM, FACEBOOK_BASE_COM)

    override fun parseImpl(doc: Document): FrostSearches? {
        val container: Element = doc.getElementById("BrowseResultsContainer")
            ?: doc.getElementById("root")
            ?: return null

        return FrostSearches(container.select("table[role=presentation]").mapNotNull { el ->
            // Our assumption is that search entries start with an image, followed by general info
            // There may be other <td />s, but we will not be parsing them
            // Furthermore, the <td /> entry wraps a link, containing all the necessary info
            val a = el.select("td")
                .getOrNull(1)
                ?.selectFirst("a")
                ?: return@mapNotNull null
            val url =
                a.attr("href").takeIf { it.isNotEmpty() }
                    ?.formattedFbUrl?.formattedSearchUrl
                    ?: return@mapNotNull null
            // Currently, children should all be <div /> elements, where the first entry is the name/title
            // And the other entries are additional info.
            // There are also cases of nested tables, eg for the "join" button in groups.
            // Those elements have <span /> texts, so we will filter by div to ignore those
            val texts = a.children().filter { it.tagName() == "div" && it.hasText() }
            val title = texts.firstOrNull()?.text() ?: return@mapNotNull null
            val info = texts.takeIf { it.size > 1 }?.last()?.text()
            L.e { a }
            create(
                href = url,
                title = title,
                description = info
            ).also { L.e { it } }
        })
    }
}
