package com.pitchedapps.frost.facebook.requests

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import okhttp3.Call
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import java.io.IOException

/**
 * Created by Allan Wang on 29/12/17.
 */
fun RequestAuth.getMenuData(): FrostRequest<MenuData?> {

    val body = listOf(
            "fb_dtsg" to fb_dtsg,
            "__user" to userId
    ).withEmptyData("m_sess", "__dyn", "__req", "__ajax__")

    return frostRequest(::parseMenu) {
        url("${FB_URL_BASE}bookmarks/flyout/body/?id=u_0_2")
        post(body.toForm())
    }

}

fun parseMenu(call: Call): MenuData? {
    val fullString = call.execute().body()?.string() ?: return null
    var jsonString = fullString.substringAfter("bookmarkGroups", "")
            .substringAfter("[", "")

    if (jsonString.isBlank()) return null

    jsonString = "{ \"data\" : [${StringEscapeUtils.unescapeEcmaScript(jsonString)}"

    val mapper = ObjectMapper()
            .disable(MapperFeature.AUTO_DETECT_SETTERS)

    return try {
        val data = mapper.readValue(jsonString, MenuData::class.java)

        // parse footer content

        val footer = fullString.substringAfter("footerMarkup", "")
                .substringAfter("{", "")
                .substringBefore("}", "")

        val doc = Jsoup.parseBodyFragment(StringEscapeUtils.unescapeEcmaScript(
                StringEscapeUtils.unescapeEcmaScript(footer)))
        val footerData = mutableListOf<MenuFooterItem>()
        val footerSmallData = mutableListOf<MenuFooterItem>()

        doc.select("a[href]").forEach {
            val text = it.text()
            it.parent()
            if (text.isEmpty()) return@forEach
            val href = it.attr("href").formattedFbUrl
            val item = MenuFooterItem(name = text, url = href)
            if (it.parent().tag().name == "span")
                footerSmallData.add(item)
            else
                footerData.add(item)
        }

        return data.copy(footer = MenuFooter(footerData, footerSmallData))
    } catch (e: IOException) {
        L.e(e) { "Menu parse fail" }
        null
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuData(val data: List<MenuHeader> = emptyList(),
                    val footer: MenuFooter = MenuFooter()) {

    @JsonCreator constructor(
            @JsonProperty("data") data: List<MenuHeader>?
    ) : this(data ?: emptyList(), MenuFooter())

    fun flatMapValid(): List<MenuItemData> {
        val items = mutableListOf<MenuItemData>()
        data.forEach {
            if (it.isValid) items.add(it)
            items.addAll(it.visible.filter(MenuItem::isValid))
        }

        items.addAll(footer.data.filter(MenuFooterItem::isValid))
        items.addAll(footer.smallData.filter(MenuFooterItem::isValid))

        return items
    }

}

interface MenuItemData {
    val isValid: Boolean
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuHeader(val id: String? = null,
                      val header: String? = null,
                      val visible: List<MenuItem> = emptyList(),
                      val all: List<MenuItem> = emptyList()) : MenuItemData {

    @JsonCreator constructor(
            @JsonProperty("id") id: String?,
            @JsonProperty("header") header: String?,
            @JsonProperty("visible") visible: List<MenuItem>?,
            @JsonProperty("all") all: List<MenuItem>?,
            @JsonProperty("fake") fake: Boolean?
    ) : this(id, header, visible ?: emptyList(), all ?: emptyList())

    override val isValid: Boolean
        get() = !header.isNullOrBlank()
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuItem(val id: String? = null,
                    val name: String? = null,
                    val pic: String? = null,
                    val url: String? = null,
                    val count: Int = 0,
                    val countDetails: String? = null) : MenuItemData {

    @JsonCreator constructor(
            @JsonProperty("id") id: String?,
            @JsonProperty("name") name: String?,
            @JsonProperty("pic") pic: String?,
            @JsonProperty("url") url: String?,
            @JsonProperty("count") count: Int?,
            @JsonProperty("count_details") countDetails: String?,
            @JsonProperty("fake") fake: Boolean?
    ) : this(id, name, pic?.formattedFbUrl, url?.formattedFbUrl, count ?: 0, countDetails)

    override val isValid: Boolean
        get() = !name.isNullOrBlank() && !url.isNullOrBlank()
}

data class MenuFooter(val data: List<MenuFooterItem> = emptyList(),
                      val smallData: List<MenuFooterItem> = emptyList()) {

    val hasContent
        get() = data.isNotEmpty() || smallData.isNotEmpty()

}

data class MenuFooterItem(val name: String? = null,
                          val url: String? = null,
                          val isSmall: Boolean = false) : MenuItemData {
    override val isValid: Boolean
        get() = name != null && url != null
}