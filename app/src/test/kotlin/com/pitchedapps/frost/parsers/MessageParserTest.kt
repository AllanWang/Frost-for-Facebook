package com.pitchedapps.frost.parsers

import org.apache.commons.text.StringEscapeUtils
import org.junit.Test

/**
 * Created by Allan Wang on 2017-10-06.
 */
class MessageParserTest {

    @Test
    fun basic() {
        val content = getResource("priv/messages.html") ?: return
        println(MessageParser.debug(content).joinToString(separator = "\n"))
    }

    @Test
    fun format() {
        var content = getResource("priv/messages.html") ?: return
        content = StringEscapeUtils.unescapeEcmaScript(content)
        val begin = content.indexOf("id=\"threadlist_rows\"")
        if (begin > 0) content = content.substring(begin)
        val end = content.indexOf("</script>")
        if (end > 0) content = content.substring(0, end)
        content = content.substringBeforeLast("</div>")
        println("<div $content</div>")
    }
}