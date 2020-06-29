package com.pitchedapps.frost.facebook.parsers

import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FB_URL_BASE
import org.jsoup.nodes.Document

object BadgeParser : FrostParser<FrostBadges> by BadgeParserImpl()

data class FrostBadges(
    val feed: String?,
    val friends: String?,
    val messages: String?,
    val notifications: String?
) : ParseData {
    override val isEmpty: Boolean
        get() = feed.isNullOrEmpty()
            && friends.isNullOrEmpty()
            && messages.isNullOrEmpty()
            && notifications.isNullOrEmpty()
}

private class BadgeParserImpl : FrostParserBase<FrostBadges>(false) {
    // Not actually displayed
    override var nameRes: Int = R.string.frost_name

    override val url: String = FB_URL_BASE

    override fun parseImpl(doc: Document): FrostBadges? {
        val header = doc.getElementById("header") ?: return null
        if (header.select("[data-sigil=count]").isEmpty())
            return null
        val (feed, requests, messages, notifications) = listOf(
            "feed",
            "requests",
            "messages",
            "notifications"
        )
            .map { "[data-sigil*=$it] [data-sigil=count]" }
            .map { doc.select(it) }
            .map { e -> e?.getOrNull(0)?.ownText() }
        return FrostBadges(
            feed = feed,
            friends = requests,
            messages = messages,
            notifications = notifications
        )
    }
}