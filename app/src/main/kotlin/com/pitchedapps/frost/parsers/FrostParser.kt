package com.pitchedapps.frost.parsers

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * Interface for a given parser
 * Use cases should be attached as delegates to objects that implement this interface
 */
interface FrostParser<T> {
    fun parse(text: String?): T?
    fun debug(text: String?): String
}

internal abstract class FrostParserBase<T> : FrostParser<T> {
    override final fun parse(text: String?): T?
            = if (text == null) null else parseImpl(text)

    protected abstract fun parseImpl(text: String): T?

    override final fun debug(text: String?): String {
        val result = mutableListOf<String>()
        result.add("Testing parser for ${this::class.java.simpleName}")
        if (text == null) {
            result.add("Input is null")
            return result.joinToString("\n")
        }
        val output = parseImpl(text)
        if (output == null) {
            result.add("Output is null")
            return result.joinToString("\n")
        }
        debugImpl(output, result)
        return result.joinToString("\n")
    }

    protected abstract fun debugImpl(data: T, result: MutableList<String>)
}

object FrostRegex {
    val epoch = Regex(":([0-9]+)")
    val notifId = Regex("notif_id\":([0-9]+)")
    val messageNotifId = Regex("thread_fbid_([0-9]+)")
    val profilePicture = Regex("url\\(\"(.*?)\"\\)")
}