package com.pitchedapps.frost.facebook.requests

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.utils.L
import okhttp3.Call
import org.apache.commons.text.StringEscapeUtils
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
    var jsonString = call.execute().body()?.string() ?: return null
    jsonString = jsonString.substringAfter("bookmarkGroups", "")
            .substringAfter("[", "")

    if (jsonString.isBlank()) return null

    jsonString = "{ \"data\" : [${StringEscapeUtils.unescapeEcmaScript(jsonString)}"

    val mapper = ObjectMapper()
            .disable(MapperFeature.AUTO_DETECT_SETTERS)

    return try {
        mapper.readValue(jsonString, MenuData::class.java)
    } catch (e: IOException) {
        L.e(e, "Menu parse fail")
        null
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuData(val data: List<MenuHeader>) {

    @JsonCreator constructor(
            @JsonProperty("data") data: List<MenuHeader>?,
            @JsonProperty("fake") fake: Boolean?
    ) : this(data ?: emptyList())

}

interface MenuItemData {
    val valid: Boolean
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuHeader(val id: String?,
                      val header: String?,
                      val visible: List<MenuItem>,
                      val all: List<MenuItem>) : MenuItemData {

    @JsonCreator constructor(
            @JsonProperty("id") id: String?,
            @JsonProperty("header") header: String?,
            @JsonProperty("visible") visible: List<MenuItem>?,
            @JsonProperty("all") all: List<MenuItem>?,
            @JsonProperty("fake") fake: Boolean?
    ) : this(id, header, visible ?: emptyList(), all ?: emptyList())

    override val valid: Boolean
        get() = header != null
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuItem(val id: String?,
                    val name: String?,
                    val pic: String?,
                    val url: String?,
                    val count: Int,
                    val countDetails: String?) : MenuItemData {

    @JsonCreator constructor(
            @JsonProperty("id") id: String?,
            @JsonProperty("name") name: String?,
            @JsonProperty("pic") pic: String?,
            @JsonProperty("url") url: String?,
            @JsonProperty("count") count: Int?,
            @JsonProperty("count_details") countDetails: String?,
            @JsonProperty("fake") fake: Boolean?
    ) : this(id, name, pic, url, count ?: 0, countDetails)

    override val valid: Boolean
        get() = name != null
}