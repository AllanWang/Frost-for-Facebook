package com.pitchedapps.frost.facebook

/**
 * Created by Allan Wang on 2017-05-29.
 */
enum class FBURL(val url: String) {
    FEED("https://touch.facebook.com/"),
    PROFILE("https://touch.facebook.com/me/"),
    BOOKMARKS("https://touch.facebook.com/bookmarks"),
    SEARCH("https://touch.facebook.com/search"),
    EVENTS("https://touch.facebook.com/events/upcoming"),
    FRIEND_REQUESTS("https://touch.facebook.com/requests"),
    MESSAGES("https://touch.facebook.com/messages"),
    NOTIFICATIONS("https://touch.facebook.com/notifications");
}