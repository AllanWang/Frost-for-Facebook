package com.pitchedapps.frost.facebook

import android.support.annotation.StringRes
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.pitchedapps.frost.R
import com.pitchedapps.frost.fragments.BaseFragment
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.fragments.WebFragmentMenu

enum class FbItem(
        @StringRes val titleId: Int,
        val icon: IIcon,
        relativeUrl: String,
        val fragmentCreator: () -> BaseFragment = ::WebFragment
) {
    ACTIVITY_LOG(R.string.activity_log, GoogleMaterial.Icon.gmd_list, "me/allactivity"),
    BIRTHDAYS(R.string.birthdays, GoogleMaterial.Icon.gmd_cake, "events/birthdays"),
    CHAT(R.string.chat, GoogleMaterial.Icon.gmd_chat, "buddylist"),
    EVENTS(R.string.events, GoogleMaterial.Icon.gmd_event_note, "events/upcoming"),
    FEED(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, ""),
    FEED_MOST_RECENT(R.string.most_recent, GoogleMaterial.Icon.gmd_history, "home.php?sk=h_chr"),
    FEED_TOP_STORIES(R.string.top_stories, GoogleMaterial.Icon.gmd_star, "home.php?sk=h_nor"),
    FRIENDS(R.string.friends, GoogleMaterial.Icon.gmd_person_add, "friends/center/requests"),
    GROUPS(R.string.groups, GoogleMaterial.Icon.gmd_group, "groups"),
    MENU(R.string.menu, GoogleMaterial.Icon.gmd_menu, "settings", ::WebFragmentMenu),
    MESSAGES(R.string.messages, MaterialDesignIconic.Icon.gmi_comments, "messages"),
    NOTES(R.string.notes, CommunityMaterial.Icon.cmd_note, "notes"),
    NOTIFICATIONS(R.string.notifications, MaterialDesignIconic.Icon.gmi_globe, "notifications"),
    ON_THIS_DAY(R.string.on_this_day, GoogleMaterial.Icon.gmd_today, "onthisday"),
    PAGES(R.string.pages, GoogleMaterial.Icon.gmd_flag, "pages"),
    PHOTOS(R.string.photos, GoogleMaterial.Icon.gmd_photo, "me/photos"),
    PROFILE(R.string.profile, CommunityMaterial.Icon.cmd_account, "me"),
    SAVED(R.string.saved, GoogleMaterial.Icon.gmd_bookmark, "saved"),
    _SEARCH(R.string.search_menu_title, GoogleMaterial.Icon.gmd_search, "search/top"),
    SETTINGS(R.string.settings, GoogleMaterial.Icon.gmd_settings, "settings"),
    ;

    val url = "$FB_URL_BASE$relativeUrl"
}

inline val fbSearch
    get() = fbSearch()

fun fbSearch(query: String = "a") = "$FB_SEARCH$query"

private const val FB_SEARCH = "${FB_URL_BASE}search/top/?q="
fun defaultTabs(): List<FbItem> = listOf(FbItem.FEED, FbItem.MESSAGES, FbItem.NOTIFICATIONS, FbItem.MENU)
