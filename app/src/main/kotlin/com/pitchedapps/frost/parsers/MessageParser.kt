package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.*
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
object MessageParser : FrostParser<FrostMessages> by MessageParserImpl()

data class FrostMessages(val threads: List<FrostThread>, val seeMore: FrostLink?, val extraLinks: List<FrostLink>) : ParseResponse {
    override fun toString() = StringBuilder().apply {
        append("FrostMessages {\n")
        append("\tthreads: [\n\t\t")
        append(threads.joinToString("\n\t\t"))
        append("\n\t]\n\n\tsee more: $seeMore\n\n")
        append("\textra links: [\n\t\t")
        append(extraLinks.joinToString("\n\t\t"))
        append("\n\t]\n}")
    }.toString()
}

/**
 * [id] user/thread id, or current time fallback
 * [img] parsed url for profile img
 * [time] time of message
 * [url] link to thread
 * [unread] true if image is unread, false otherwise
 * [content] optional string for thread
 */
data class FrostThread(val id: Long, val img: String, val title: String, val time: Long, val url: String, val unread: Boolean, val content: String?)

data class FrostLink(val text: String, val href: String)

private class MessageParserImpl : FrostParserBase<FrostMessages>(true) {

    override val url = FbItem.MESSAGES.url

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

    override fun parse(doc: Document): FrostMessages? {
        val threadList = doc.getElementById("threadlist_rows") ?: return null
        val threads: List<FrostThread> = threadList.getElementsByAttributeValueContaining("id", "thread_fbid_")
                .mapNotNull { parseMessage(it) }
        val seeMore = parseLink(doc.getElementById("see_older_threads"))
        val extraLinks = threadList.nextElementSibling().select("a")
                .mapNotNull { parseLink(it) }
        return FrostMessages(threads, seeMore, extraLinks)
    }

    private fun parseMessage(element: Element): FrostThread? {
        val a = element.getElementsByTag("a").first() ?: return null
        val abbr = element.getElementsByTag("abbr")
        val epoch = FB_EPOCH_MATCHER.find(abbr.attr("data-store"))[1]?.toLongOrNull() ?: -1L
        //fetch id
        val id = FB_MESSAGE_NOTIF_ID_MATCHER.find(element.id())[1]?.toLongOrNull()
                ?: System.currentTimeMillis()
        val content = element.select("span.snippet").firstOrNull()?.text()?.trim()
        val img = element.getInnerImgStyle()
        L.v("url", a.attr("href"))
        return FrostThread(
                id = id,
                img = img,
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
}
