package com.pitchedapps.frost.parsers

/**
 * Created by Allan Wang on 2017-10-06.
 */
interface FrostParser<T> {
    fun match(text: String?): Sequence<MatchResult>?
    fun parse(text: String?): T?
    fun debug(text: String?): List<String>
}

/**
 * Hidden implementation of [FrostParser]
 * This should be extended, and objects should expose only the interface, using the extension as a delegate
 */
internal abstract class FrostParserBase<T>(private val matcher: Regex) : FrostParser<T> {
    override final fun match(text: String?): Sequence<MatchResult>? = if (text == null) null else matcher.findAll(text)

    override final fun parse(text: String?): T? {
        val matches = match(text) ?: return null
        return parseImpl(matches)
    }

    protected abstract fun parseImpl(matches: Sequence<MatchResult>): T

    override final fun debug(text: String?): List<String> {
        val result = mutableListOf<String>()
        result.add("Debugging text of length ${text?.length ?: "null"}")
        val matches = match(text)
        if (matches == null) {
            result.add("Match returned null")
            return result
        }
        result.add("Matched ${matches.count()} bundles")
        debugImpl(parseImpl(matches), result)
        return result
    }

    protected abstract fun debugImpl(output: T, result: MutableList<String>)
}

enum class FrostRegex(val data: () -> Array<String>) {
    /**
     * Format:
     * id=\"threadlist_rows\"[thread_data] ...
     * id=\"see_older_threads\"[see_more] ...
     * marea ... [extra_links] ...
     * </script>
     */
    MESSAGES({
        arrayOf(
                //thread data 1
                """id\\"threadlist_rows\\"""",
                "(.*?)",
                //see more 2
                """.*?id\\"see_older_threads\\"""",
                "(.*?)",
                //extra links 3
                """marea""",
                "(.*?)",
                """<\\/script>"""
        )
    }),
    /**
     * Format:
     * id=\"threadlist_row_ ...
     * style=\"background:#d8dce6 url(&quot;[bg]&quot;) ...
     * \u003Cstrong>[title]\u003C\/strong> ...
     * snippet\">[content]\u003C\
     * data-store=\"&#123;&quot;time&quot;:[time],&quot; ...
     * href=\"[relative_href]\">[title]\u003C\/a> ...
     * div class=\"[classes]\"
     */
    THREAD({
        arrayOf(
                //bg 1
                """id=\\"threadlist_row_.*?background.*?url\(&quot;""",
                "(.*?)",
                """&quot;""",
                //title 2
                """.*?strong>""",
                "(.*?)",
                """\\u003C\\/strong""",
                ///content 3
                """.*?snippet\\">""",
                "(.*?)",
                "\\u003C",
                //time 4
                """time&quote;:""",
                "([0-9]*)",
                //relative href 5
                """href=\\"""",
                "(.*?)",
                """\\">""",
                //title 6
                "(.*?)",
                """\\u003C\\/a>""",
                //classes 7
                """.*?class=\\"""",
                "(.*?)",
                "\""
        )
    }),
    /**
     * Format
     * href=\"[url]\">[text]\u003C\/a>
     */
    LINK({
        arrayOf(
                """href=\\">""",
                "(.*?)",
                """\\u003C"""
        )
    });

    val matcher: Regex by lazy { Regex(data().joinToString(separator = "")) }
}