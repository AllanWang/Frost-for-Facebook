package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.FB_CSS_URL_MATCHER
import com.pitchedapps.frost.facebook.FbCookie
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
     * Directly load from jsoup with given cookie
     */
    fun fromJsoup(cookie: String?): T?

    /**
     * Extracts data from the JSoup document
     * In some cases, the document can be created directly from a connection
     * In other times, it needs to be created from scripts, which otherwise
     * won't be parsed
     */
    fun parse(doc: Document): T?

    /**
     * Parse a String input
     */
    fun parse(text: String?): T?

}

/**
 * Used as extensions as it can't be mocked in unit tests
 */
fun <T : Any> FrostParser<T>.fromJsoup() = fromJsoup(FbCookie.webCookie)

internal interface ParseResponse {
    override fun toString(): String
}

/**
 * T should have a readable toString() function
 */
internal abstract class FrostParserBase<out T : ParseResponse> : FrostParser<T> {

    override fun fromJsoup(cookie: String?) = parse(frostJsoup(cookie, url))

    /**
     * Retrieve fromJsoup, but pass through text converter
     */
    protected fun fromJsoupThroughText(cookie: String?) =
            parse(frostJsoup(cookie, url).toString())

    override final fun parse(text: String?): T? {
        text ?: return null
        val doc = textToDoc(text) ?: return null
        return parse(doc)
    }

    /**
     * Attempts to find inner <i> element with some style containing a url
     * Returns the formatted url, or an empty string if nothing was found
     */
    protected fun Element.getInnerImgStyle() =
            FB_CSS_URL_MATCHER.find(select("i.img[style*=url]").attr("style"))[1]?.formattedFbUrl ?: ""

    protected abstract fun textToDoc(text: String): Document?

}