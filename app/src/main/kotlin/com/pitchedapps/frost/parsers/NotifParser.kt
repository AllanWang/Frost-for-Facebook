package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.*
import com.pitchedapps.frost.services.NotificationContent
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-12-25.
 *
 */
object NotifParser : FrostParser<FrostNotifs> by NotifParserImpl()

data class FrostNotifs(
        val notifs: List<FrostNotif>,
        val seeMore: FrostLink?
) : ParseNotification {
    override fun toString() = StringBuilder().apply {
        append("FrostNotifs {\n")
        append(notifs.toJsonString("notifs", 1))
        append("\tsee more: $seeMore\n")
        append("}")
    }.toString()

    override fun getUnreadNotifications(data: CookieModel) =
            notifs.filter(FrostNotif::unread).map {
                with(it) {
                    NotificationContent(
                            data = data,
                            id = id,
                            href = url,
                            title = null,
                            text = content,
                            timestamp = time,
                            profileUrl = img
                    )
                }
            }
}

/**
 * [id] notif id, or current time fallback
 * [img] parsed url for profile img
 * [time] time of message
 * [url] link to thread
 * [unread] true if image is unread, false otherwise
 * [content] optional string for thread
 * [timeString] text version of time from Facebook
 * [thumbnailUrl] optional thumbnail url if existent
 */
data class FrostNotif(val id: Long,
                      val img: String?,
                      val time: Long,
                      val url: String,
                      val unread: Boolean,
                      val content: String,
                      val timeString: String,
                      val thumbnailUrl: String?)

private class NotifParserImpl : FrostParserBase<FrostNotifs>(false) {

    override val url = FbItem.NOTIFICATIONS.url

    override fun parseImpl(doc: Document): FrostNotifs? {
        val notificationList = doc.getElementById("notifications_list") ?: return null
        val notifications = notificationList.getElementsByAttributeValueContaining("id", "list_notif_")
                .mapNotNull(this::parseNotif)
        val seeMore = parseLink(doc.getElementsByAttributeValue("href", "/notifications.php?more").first())
        return FrostNotifs(notifications, seeMore)
    }

    private fun parseNotif(element: Element): FrostNotif? {
        val a = element.getElementsByTag("a").first() ?: return null
        val abbr = element.getElementsByTag("abbr")
        val epoch = FB_EPOCH_MATCHER.find(abbr.attr("data-store"))[1]?.toLongOrNull() ?: -1L
        //fetch id
        val id = FB_NOTIF_ID_MATCHER.find(element.id())[1]?.toLongOrNull()
                ?: System.currentTimeMillis() % FALLBACK_TIME_MOD
        val img = element.getInnerImgStyle()
        val timeString = abbr.text()
        val content = a.text().replace("\u00a0", " ").removeSuffix(timeString).trim() //remove &nbsp;
        val thumbnail = element.selectFirst("img.thumbnail")?.attr("src")
        return FrostNotif(
                id = id,
                img = img,
                time = epoch,
                url = a.attr("href").formattedFbUrl,
                unread = !element.hasClass("acw"),
                content = content,
                timeString = timeString,
                thumbnailUrl = if (thumbnail?.isNotEmpty() == true) thumbnail else null
        )
    }


}
