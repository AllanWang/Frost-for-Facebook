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
     * Call parsing with default implementation
     */
    fun parse(cookie: String?): T?

    /**
     * Call parsing with given document
     */
    fun parse(document: Document): T?

    /**
     * Call parsing with given data
     */
    fun parseFromData(text: String): T?

}

/**
 * Used as extensions as it can't be mocked in unit tests
 */
fun <T : Any> FrostParser<T>.parse() = parse(FbCookie.webCookie)

internal interface ParseResponse {
    override fun toString(): String

    fun <T> List<T>.toJsonString(tag: String, indent: Int) = StringBuilder().apply {
        val tabs = "\t".repeat(indent)
        append("$tabs$tag: [\n\t$tabs")
        append(this@toJsonString.joinToString("\n\t$tabs"))
        append("\n$tabs]\n")
    }.toString()
}

/**
 * T should have a readable toString() function
 * [redirectToText] dictates whether all data should be converted to text then back to document before parsing
 */
internal abstract class FrostParserBase<out T : ParseResponse>(private val redirectToText: Boolean) : FrostParser<T> {

    override final fun parse(cookie: String?) = parse(frostJsoup(cookie, url))

    override final fun parseFromData(text: String): T? {
        val doc = textToDoc(text) ?: return null
        return parseImpl(doc)
    }

    override fun parse(document: Document) = if (redirectToText)
        parseFromData(document.toString())
    else
        parseImpl(document)

    protected abstract fun parseImpl(doc: Document): T?

    //    protected abstract fun parse(doc: Document): T?

    /**
     * Attempts to find inner <i> element with some style containing a url
     * Returns the formatted url, or an empty string if nothing was found
     */
    protected fun Element.getInnerImgStyle() =
            FB_CSS_URL_MATCHER.find(select("i.img[style*=url]").attr("style"))[1]?.formattedFbUrl ?: ""

    protected abstract fun textToDoc(text: String): Document?

}