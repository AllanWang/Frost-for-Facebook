package com.pitchedapps.frost.facebook

import android.content.Context
import android.support.annotation.StringRes
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.pitchedapps.frost.R

enum class FbTab(@StringRes val titleId: Int, val icon: IIcon, relativeUrl: String) {
//    LOGIN(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, "https://www.facebook.com/v2.9/dialog/oauth?client_id=${FB_KEY}&redirect_uri=https://touch.facebook.com/&response_type=token,granted_scopes"),
    FEED(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, ""),
    PROFILE(R.string.profile, CommunityMaterial.Icon.cmd_account, "me"),
    EVENTS(R.string.events, GoogleMaterial.Icon.gmd_event, "events/upcoming"),
    FRIENDS(R.string.friends, GoogleMaterial.Icon.gmd_people, "friends/center/requests"),
    MESSAGES(R.string.messages, MaterialDesignIconic.Icon.gmi_comments, "messages?disable_interstitial=1&rdr"),
    NOTIFICATIONS(R.string.notifications, MaterialDesignIconic.Icon.gmi_globe, "notifications");

    val url = "$FB_URL_BASE$relativeUrl"
}

const val FACEBOOK_COM = "facebook.com"
const val FB_URL_BASE = "https://m.facebook.com/"