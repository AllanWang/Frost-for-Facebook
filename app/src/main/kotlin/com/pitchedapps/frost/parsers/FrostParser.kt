package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FB_CSS_URL_MATCHER
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.utils.frostJsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

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
    fun parse(cookie: CookieModel): ParseResp<T>?

    /**
     * Call parsing with given document
     */
    fun parse(cookie: CookieModel, document: Document): ParseResp<T>?

    /**
     * Call parsing with given data
     */
    fun parseFromData(cookie: CookieModel, text: String): ParseResp<T>?

}

data class ParseResp<out T>(val cookie: CookieModel, val data: T) {
    override fun toString() = "ParseResp\ncookie: $cookie\ndata:\n$data"
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

    override final fun parse(cookie: CookieModel) = parse(cookie, frostJsoup(cookie.cookie, url))

    override final fun parseFromData(cookie: CookieModel, text: String): ParseResp<T>? {
        val doc = textToDoc(text) ?: return null
        val data = parseImpl(cookie, doc) ?: return null
        return ParseResp(cookie, data)
    }

    override fun parse(cookie: CookieModel, document: Document): ParseResp<T>? {
        if (redirectToText)
            return parseFromData(cookie, document.toString())
        val data = parseImpl(cookie, document) ?: return null
        return ParseResp(cookie, data)
    }

    protected abstract fun parseImpl(cookie: CookieModel, doc: Document): T?

    //    protected abstract fun parse(doc: Document): T?

    /**
     * Attempts to find inner <i> element with some style containing a url
     * Returns the formatted url, or an empty string if nothing was found
     */
    protected fun Element.getInnerImgStyle() =
            FB_CSS_URL_MATCHER.find(select("i.img[style*=url]").attr("style"))[1]?.formattedFbUrl ?: ""

    protected abstract fun textToDoc(text: String): Document?

}