package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FB_CSS_URL_MATCHER
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.services.NotificationContent
import com.pitchedapps.frost.utils.frostJsoup
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * Interface for a given parser
 * Use cases should be attached as delegates to objects that implement this interface
 *
 * In all cases, parsing will be done from a JSoup document
 * Variants accepting strings are also permitted, and they will be converted to documents accordingly
 * The return type must be nonnull if no parsing errors occurred, as null signifies a parse error
 * If null really must be allowed, use Optionals
 */
interface FrostParser<out T : Any> {

    /**
     * Url to request from
     */
    val url: String

    /**
     * Call parsing with default implementation using cookie
     */
    fun parse(cookie: String?): ParseResponse<T>?

    /**
     * Call parsing with given document
     */
    fun parse(cookie: String?, document: Document): ParseResponse<T>?

    /**
     * Call parsing using jsoup to fetch from given url
     */
    fun parseFromUrl(cookie: String?, url: String): ParseResponse<T>?

    /**
     * Call parsing with given data
     */
    fun parseFromData(cookie: String?, text: String): ParseResponse<T>?

}

const val FALLBACK_TIME_MOD = 1000000

data class FrostLink(val text: String, val href: String)

data class ParseResponse<out T>(val cookie: String, val data: T) {
    override fun toString() = "ParseResponse\ncookie: $cookie\ndata:\n$data"
}

interface ParseNotification {
    fun getUnreadNotifications(data: CookieModel): List<NotificationContent>
}

internal fun <T> List<T>.toJsonString(tag: String, indent: Int) = StringBuilder().apply {
    val tabs = "\t".repeat(indent)
    append("$tabs$tag: [\n\t$tabs")
    append(this@toJsonString.joinToString("\n\t$tabs"))
    append("\n$tabs]\n")
}.toString()

/**
 * T should have a readable toString() function
 * [redirectToText] dictates whether all data should be converted to text then back to document before parsing
 */
internal abstract class FrostParserBase<out T : Any>(private val redirectToText: Boolean) : FrostParser<T> {

    override final fun parse(cookie: String?) = parseFromUrl(cookie, url)

    override final fun parseFromData(cookie: String?, text: String): ParseResponse<T>? {
        cookie ?: return null
        val doc = textToDoc(text) ?: return null
        val data = parseImpl(doc) ?: return null
        return ParseResponse(cookie, data)
    }

    override final fun parseFromUrl(cookie: String?, url: String): ParseResponse<T>? =
            parse(cookie, frostJsoup(cookie, url))

    override fun parse(cookie: String?, document: Document): ParseResponse<T>? {
        cookie ?: return null
        if (redirectToText)
            return parseFromData(cookie, document.toString())
        val data = parseImpl(document) ?: return null
        return ParseResponse(cookie, data)
    }

    protected abstract fun parseImpl(doc: Document): T?

    //    protected abstract fun parse(doc: Document): T?

    /**
     * Attempts to find inner <i> element with some style containing a url
     * Returns the formatted url, or an empty string if nothing was found
     */
    protected fun Element.getInnerImgStyle() =
            select("i.img[style*=url]").getStyleUrl()

    protected fun Elements.getStyleUrl() =
            FB_CSS_URL_MATCHER.find(attr("style"))[1]?.formattedFbUrl

    protected open fun textToDoc(text: String) = if (!redirectToText)
        Jsoup.parse(text)
    else
        throw RuntimeException("${this::class.java.simpleName} requires text redirect but did not implement textToDoc")

    protected fun parseLink(element: Element?): FrostLink? {
        val a = element?.getElementsByTag("a")?.first() ?: return null
        return FrostLink(a.text(), a.attr("href"))
    }
}