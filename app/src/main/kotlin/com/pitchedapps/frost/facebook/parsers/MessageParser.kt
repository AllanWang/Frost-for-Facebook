package com.pitchedapps.frost.facebook.parsers

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.*
import com.pitchedapps.frost.services.NotificationContent
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
object MessageParser : FrostParser<FrostMessages> by MessageParserImpl() {

    fun queryUser(cookie: String?, name: String) = parseFromUrl(cookie, "${FbItem.MESSAGES.url}/?q=$name")

}

data class FrostMessages(val threads: List<FrostThread>,
                         val seeMore: FrostLink?,
                         val extraLinks: List<FrostLink>
) : ParseNotification {
    override fun toString() = StringBuilder().apply {
        append("FrostMessages {\n")
        append(threads.toJsonString("threads", 1))
        append("\tsee more: $seeMore\n")
        append(extraLinks.toJsonString("extra links", 1))
        append("}")
    }.toString()

    override fun getUnreadNotifications(data: CookieModel) =
            threads.filter(FrostThread::unread).map {
                with(it) {
                    NotificationContent(
                            data = data,
                            id = id,
                            href = url,
                            title = title,
                            text = content ?: "",
                            timestamp = time,
                            profileUrl = img
                    )
                }
            }
}

/**
 * [id] user/thread id, or current time fallback
 * [img] parsed url for profile img
 * [time] time of message
 * [url] link to thread
 * [unread] true if image is unread, false otherwise
 * [content] optional string for thread
 */
data class FrostThread(val id: Long,
                       val img: String?,
                       val title: String,
                       val time: Long,
                       val url: String,
                       val unread: Boolean,
                       val content: String?,
                       val contentImgUrl: String?)

private class MessageParserImpl : FrostParserBase<FrostMessages>(true) {

    override var nameRes = FbItem.MESSAGES.titleId

    override val url = FbItem.MESSAGES.url

    override fun textToDoc(text: String): Document? {
        var content = StringEscapeUtils.unescapeEcmaScript(text)
        val begin = content.indexOf("id=\"threadlist_rows\"")
        if (begin <= 0) {
            L.d { "Threadlist not found" }
            return null
        }
        content = content.substring(begin)
        val end = content.indexOf("</script>")
        if (end <= 0) {
            L.d { "Script tail not found" }
            return null
        }
        content = content.substring(0, end).substringBeforeLast("</div>")
        return Jsoup.parseBodyFragment("<div $content")
    }

    override fun parseImpl(doc: Document): FrostMessages? {
        val threadList = doc.getElementById("threadlist_rows") ?: return null
        val threads: List<FrostThread> = threadList.getElementsByAttributeValueContaining("id", "thread_fbid_")
                .mapNotNull(this::parseMessage)
        val seeMore = parseLink(doc.getElementById("see_older_threads"))
        val extraLinks = threadList.nextElementSibling().select("a")
                .mapNotNull(this::parseLink)
        return FrostMessages(threads, seeMore, extraLinks)
    }

    private fun parseMessage(element: Element): FrostThread? {
        val a = element.getElementsByTag("a").first() ?: return null
        val abbr = element.getElementsByTag("abbr")
        val epoch = FB_EPOCH_MATCHER.find(abbr.attr("data-store"))[1]?.toLongOrNull() ?: -1L
        //fetch id
        val id = FB_MESSAGE_NOTIF_ID_MATCHER.find(element.id())[1]?.toLongOrNull()
                ?: System.currentTimeMillis() % FALLBACK_TIME_MOD
        val snippet = element.select("span.snippet").firstOrNull()
        val content = snippet?.text()?.trim()
        val contentImg = snippet?.select("i[style*=url]")?.getStyleUrl()
        val img = element.getInnerImgStyle()
        return FrostThread(
                id = id,
                img = img,
                title = a.text(),
                time = epoch,
                url = a.attr("href").formattedFbUrl,
                unread = !element.hasClass("acw"),
                content = content,
                contentImgUrl = contentImg
        )
    }

}
