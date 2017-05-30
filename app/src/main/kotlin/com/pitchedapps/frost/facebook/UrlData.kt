package com.pitchedapps.frost.facebook

import android.content.Context
import android.support.annotation.StringRes
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.RealmFiles
import com.pitchedapps.frost.utils.realm
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Allan Wang on 2017-05-29.
 */
enum class FbUrl(@StringRes val titleId: Int, val icon: IIcon, val url: String) {
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

class FbTab(var title: String, var icon: IIcon, var url: String) {
    constructor(realm: FbTabRealm) : this(realm.title, when (realm.iconCategory) {
        0 -> GoogleMaterial.Icon.valueOf(realm.iconString)
        1 -> CommunityMaterial.Icon.valueOf(realm.iconString)
        2 -> MaterialDesignIconic.Icon.valueOf(realm.iconString)
        else -> GoogleMaterial.Icon.gmd_error
    }, realm.url)
}

open class FbTabRealm(var title: String, var iconCategory: Int, var iconString: String, @PrimaryKey var url: String) : RealmObject() {
    constructor(tab: FbTab) : this(tab.title, when (tab.icon.typeface) {
        is GoogleMaterial -> 0
        is CommunityMaterial -> 1
        is MaterialDesignIconic -> 2
        else -> -1
    }, tab.icon.toString(), tab.url)

    constructor() : this("", -1, "", "")
}

fun List<FbTab>.save() {
    val list = RealmList(*this.map { FbTabRealm(it) }.toTypedArray())
    realm(RealmFiles.TABS, Realm.Transaction { it.copyToRealmOrUpdate(list) })
}

fun loadFbTab(c: Context): List<FbTab> {
    val realmList = mutableListOf<FbTabRealm>()
    realm(RealmFiles.TABS, Realm.Transaction { it.copyFromRealm(realmList) })
    if (realmList.isNotEmpty()) return realmList.map { FbTab(it) }
    return FbUrl.values().map { it.tabInfo(c) }
}

