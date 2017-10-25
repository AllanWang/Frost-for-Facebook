package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * In Facebook, messages are passed through scripts and loaded into view via react afterwards
 * We can parse out the content we want directly and load it ourselves
 *
 */
object MessageParser : FrostParser<Triple<List<FrostThread>, FrostLink?, List<FrostLink>>> by MessageParserImpl()

data class FrostThread(val id: Int, val img: String, val title: String, val time: Long, val url: String, val unread: Boolean, val content: String?)

data class FrostLink(val text: String, val href: String)

private class MessageParserImpl : FrostParserBase<Triple<List<FrostThread>, FrostLink?, List<FrostLink>>>() {

    override fun textToDoc(text: String): Document? {
        var content = StringEscapeUtils.unescapeEcmaScript(text)
        val begin = content.indexOf("id=\"threadlist_rows\"")
        if (begin <= 0) {
            L.d("Threadlist not found")
            return null
        }
        content = content.substring(begin)
        val end = content.indexOf("</script>")
        if (end <= 0) {
            L.d("Script tail not found")
            return null
        }
        content = content.substring(0, end).substringBeforeLast("</div>")
        return Jsoup.parseBodyFragment("<div $content")
    }

    override fun parse(doc: Document): Triple<List<FrostThread>, FrostLink?, List<FrostLink>>? {
        val threadList = doc.getElementById("threadlist_rows")
        val threads: List<FrostThread> = threadList.getElementsByAttributeValueContaining("id", "thread_fbid_")
                .mapNotNull { parseMessage(it) }
        val seeMore = parseLink(doc.getElementById("see_older_threads"))
        val extraLinks = threadList.nextElementSibling().select("a")
                .mapNotNull { parseLink(it) }
        return Triple(threads, seeMore, extraLinks)
    }

    private fun parseMessage(element: Element): FrostThread? {
        val a = element.getElementsByTag("a").first() ?: return null
        val abbr = element.getElementsByTag("abbr")
        val epoch = FrostRegex.epoch.find(abbr.attr("data-store"))
                ?.groupValues?.getOrNull(1)?.toLongOrNull() ?: -1L
        //fetch id
        val id = FrostRegex.messageNotifId.find(element.id())
                ?.groupValues?.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis()
        val content = element.select("span.snippet").firstOrNull()?.text()?.trim()
        //fetch convo pic
        val p = element.select("i.img[style*=url]")
        val pUrl = FrostRegex.profilePicture.find(p.attr("style"))?.groups?.get(1)?.value?.formattedFbUrl ?: ""
        L.v("url", a.attr("href"))
        return FrostThread(
                id = id.toInt(),
                img = pUrl.formattedFbUrl,
                title = a.text(),
                time = epoch,
                url = a.attr("href").formattedFbUrl,
                unread = !element.hasClass("acw"),
                content = content
        )
    }

    private fun parseLink(element: Element?): FrostLink? {
        val a = element?.getElementsByTag("a")?.first() ?: return null
        return FrostLink(a.text(), a.attr("href"))
    }

    override fun debugImpl(data: Triple<List<FrostThread>, FrostLink?, List<FrostLink>>, result: MutableList<String>) {
        result.addAll(data.first.map(FrostThread::toString))
        result.add("See more link:")
        result.add("\t${data.second}")
        result.addAll(data.third.map(FrostLink::toString))
    }
}
