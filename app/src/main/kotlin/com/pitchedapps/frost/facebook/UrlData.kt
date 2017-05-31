package com.pitchedapps.frost.facebook

import android.content.Context
import android.support.annotation.StringRes
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.FbTab

/**
 * Created by Allan Wang on 2017-05-29.
 */
enum class FbUrl(@StringRes val titleId: Int, val icon: IIcon, val url: String) {
    LOGIN(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, "https://www.facebook.com/v2.9/dialog/oauth?client_id=$FB_KEY&redirect_uri=https://touch.facebook.com/&response_type=token,granted_scopes"),
    FEED(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, "https://touch.facebook.com/"),
    PROFILE(R.string.profile, CommunityMaterial.Icon.cmd_account, "https://touch.facebook.com/me/"),
    EVENTS(R.string.events, GoogleMaterial.Icon.gmd_event, "https://touch.facebook.com/events/upcoming"),
    FRIENDS(R.string.friends, GoogleMaterial.Icon.gmd_people, "https://touch.facebook.com/friends/center/requests/"),
    MESSAGES(R.string.messages, MaterialDesignIconic.Icon.gmi_comments, "https://touch.facebook.com/messages"),
    NOTIFICATIONS(R.string.notifications, MaterialDesignIconic.Icon.gmi_globe, "https://touch.facebook.com/notifications");

    fun tabInfo(c: Context) = FbTab(c.getString(titleId), icon, url)
}

//BOOKMARKS("https://touch.facebook.com/bookmarks"),
//SEARCH("https://touch.facebook.com/search"),