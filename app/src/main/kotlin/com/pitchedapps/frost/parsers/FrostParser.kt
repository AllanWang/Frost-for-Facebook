package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.frostJsoup
import org.jsoup.nodes.Document

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
interface FrostParser<out T> {

    /**
     * Url to request from
     */
    val url: String

    /**
     * Directly load from jsoup with default cookie
     */
    fun fromJsoup(): T?

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

    fun debugFromJsoup(): String

    fun debugFromJsoup(cookie: String?): String

    /**
     * Take in doc and emit debug output
     */
    fun debug(doc: Document): String

    /**
     * Attempts to parse input and emit a debugger
     */
    fun debug(text: String?): String
}

internal interface ParseResponse {
    override fun toString(): String
}

/**
 * T should have a readable toString() function
 */
internal abstract class FrostParserBase<out T : ParseResponse> : FrostParser<T> {

    override final fun fromJsoup() = fromJsoup(FbCookie.webCookie)

    override final fun fromJsoup(cookie: String?) = parse(frostJsoup(cookie, url))

    override final fun parse(text: String?): T? {
        text ?: return null
        val doc = textToDoc(text) ?: return null
        return parse(doc)
    }

    protected abstract fun textToDoc(text: String): Document?

    override fun debug(text: String?): String {
        val result = mutableListOf<String>()
        result.add("Testing parser for ${this::class.java.simpleName}")
        if (text == null) {
            result.add("Null text input")
            return result.joinToString("\n")
        }
        val doc = textToDoc(text)
        if (doc == null) {
            result.add("Null document from text")
            return result.joinToString("\n")
        }
        return debug(doc, result)
    }

    override final fun debugFromJsoup() = debugFromJsoup(FbCookie.webCookie)

    override final fun debugFromJsoup(cookie: String?) = debug(frostJsoup(cookie, url))

    override final fun debug(doc: Document): String {
        val result = mutableListOf<String>()
        result.add("Testing parser for ${this::class.java.simpleName}")
        return debug(doc, result)
    }

    private fun debug(doc: Document, result: MutableList<String>): String {
        val output = parse(doc)
        if (output == null) {
            result.add("Output is null")
            return result.joinToString("\n")
        } else {
            result.add("Output is not null")
        }
        result.add(output.toString())
        return result.joinToString("\n")
    }

}