package com.pitchedapps.frost.dbflow

import android.content.Context
import android.support.annotation.StringRes
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FB_KEY
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import java.io.Serializable

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = FbTabsDb.NAME, version = FbTabsDb.VERSION)
object FbTabsDb {
    const val NAME = "FrostTabs"
    const val VERSION = 1
}

data class FbTab(val title: String, val icon: IIcon, val url: String)

@Table(database = FbTabsDb::class, allFields = true)
data class FbTabModel(
        var title: String = "Home",
        @ForeignKey var icon: IIconModel = IIconModel(),
        @PrimaryKey var url: String = ""
) : BaseModel(), Serializable {
    constructor(fbTab: FbTab) : this(fbTab.title, IIconModel(fbTab.icon), fbTab.url)

    fun toFbTab() = FbTab(title, icon.toIIcon(), url)
}

@Table(database = FbTabsDb::class, allFields = true)
data class IIconModel(var type: Int = -1, @PrimaryKey var name: String = "") : BaseModel(), Serializable {
    constructor(icon: IIcon) : this(when (icon) {
        is CommunityMaterial -> 0
        is GoogleMaterial -> 1
        is MaterialDesignIconic -> 2
        else -> -1
    }, icon.toString())

    fun toIIcon(): IIcon = when (type) {
        0 -> CommunityMaterial.Icon.valueOf(name)
        1 -> GoogleMaterial.Icon.valueOf(name)
        2 -> MaterialDesignIconic.Icon.valueOf(name)
        else -> CommunityMaterial.Icon.cmd_newspaper
    }
}

enum class FbUrl(@StringRes val titleId: Int, val icon: IIcon, val url: String) {
    LOGIN(R.string.feed, CommunityMaterial.Icon.cmd_newspaper, "https://www.facebook.com/v2.9/dialog/oauth?client_id=${FB_KEY}&redirect_uri=https://touch.facebook.com/&response_type=token,granted_scopes"),
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

fun loadFbTabs(c: Context): List<FbTab> {
    val tabs = SQLite.select()
            .from(FbTabModel::class)
            .queryList()
    if (tabs.isNotEmpty()) return tabs.map { it.toFbTab() }
    return listOf(FbUrl.FEED, FbUrl.MESSAGES, FbUrl.FRIENDS, FbUrl.NOTIFICATIONS).map { it.tabInfo(c) }
}

fun List<FbTab>.saveAsync() {

}