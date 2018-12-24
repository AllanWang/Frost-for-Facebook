package com.pitchedapps.frost.facebook.parsers

import ca.allanwang.kau.searchview.SearchItem
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.parsers.FrostSearch.Companion.create
import com.pitchedapps.frost.utils.L
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-10-09.
 */
object SearchParser : FrostParser<FrostSearches> by SearchParserImpl() {
    fun query(cookie: String?, input: String): ParseResponse<FrostSearches>? {
        val url = "${FbItem._SEARCH.url}?q=${if (input.isNotBlank()) input else "a"}"
        L._i { "Search Query $url" }
        return parseFromUrl(cookie, url)
    }
}

enum class SearchKeys(val key: String) {
    USERS("keywords_users"),
    EVENTS("keywords_events")
}

data class FrostSearches(val results: List<FrostSearch>) {

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

    override var nameRes = FbItem._SEARCH.titleId

    override val url = "${FbItem._SEARCH.url}?q=a"

    override fun parseImpl(doc: Document): FrostSearches? {
        val container: Element = doc.getElementById("BrowseResultsContainer")
            ?: doc.getElementById("root")
            ?: return null
        /**
         *
         * Removed [data-store*=result_id]
         */
        return FrostSearches(container.select("a.touchable[href]").filter(Element::hasText).map {
            FrostSearch.create(
                it.attr("href").formattedFbUrl,
                it.select("._uoi").first()?.text() ?: "",
                it.select("._1tcc").first()?.text()
            )
        }.filter { it.title.isNotBlank() })
    }
}