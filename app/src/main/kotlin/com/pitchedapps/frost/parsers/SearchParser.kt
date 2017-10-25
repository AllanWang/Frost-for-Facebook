package com.pitchedapps.frost.parsers

import ca.allanwang.kau.utils.withMaxLength
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-10-09.
 */
object SearchParser : FrostParser<List<FrostSearch>> by SearchParserImpl() {
    fun query(input: String): List<FrostSearch>? {
        val url = "${FbItem._SEARCH.url}?q=${if (input.isNotBlank()) input else "a"}"
        L.i(null, "Search Query $url")
        return parse(frostJsoup(url))
    }
}

enum class SearchKeys(val key: String) {
    USERS("keywords_users"),
    EVENTS("keywords_events")
}

/**
 * As far as I'm aware, all links are independent, so the queries don't matter
 * A lot of it is tracking information, which I'll strip away
 * Other text items are formatted for safety
 */
class FrostSearch(href: String, title: String, description: String?) {
    val href = with(href.indexOf("?")) { if (this == -1) href else href.substring(0, this) }
    val title = title.format()
    val description = description?.format()

    private fun String.format() = replace("\n", " ").withMaxLength(50)

    override fun toString(): String
            = "FrostSearch(href=$href, title=$title, description=$description)"

}

private class SearchParserImpl : FrostParserBase<List<FrostSearch>>() {
    override fun parse(doc: Document): List<FrostSearch>? {
        val container: Element = doc.getElementById("BrowseResultsContainer")
                ?: doc.getElementById("root")
                ?: return null
        /**
         *
         * Removed [data-store*=result_id]
         */
        return container.select("a.touchable[href]").filter(Element::hasText).map {
            FrostSearch(it.attr("href").formattedFbUrl,
                    it.select("._uoi").first()?.text() ?: "",
                    it.select("._1tcc").first()?.text())
        }.filter { it.title.isNotBlank() }
    }


    override fun textToDoc(text: String): Document? = Jsoup.parse(text)

    override fun debugImpl(data: List<FrostSearch>, result: MutableList<String>) {
        result.add("Has size ${data.size}")
        result.addAll(data.map(FrostSearch::toString))
    }

}