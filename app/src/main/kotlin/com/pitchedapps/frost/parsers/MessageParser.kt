package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.formattedFbUrl

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * In Facebook, messages are passed through scripts and loaded into view via react afterwards
 * We can parse out the content we want directly and load it ourselves
 *
 * Uses [FrostRegex.MESSAGES], [FrostRegex.THREAD], [FrostRegex.LINK]
 */
object MessageParser : FrostParser<Pair<List<FrostThread>, List<FrostLink>>> by MessageParserImpl()

data class FrostThread(val img: String, val title: String, val time: Long, val url: String, val unread: Boolean, val content: String)

data class FrostLink(val text: String, val href: String)

private class MessageParserImpl : FrostParserBase<Pair<List<FrostThread>, List<FrostLink>>>(FrostRegex.MESSAGES.matcher) {

    override fun parseImpl(matches: Sequence<MatchResult>): Pair<List<FrostThread>, List<FrostLink>> {
        val threads: List<FrostThread> = matches.map { FrostRegex.THREAD.matcher.findAll(it.groupValues[1]) }
                .flatten().map {
            with(it.groupValues) {
                FrostThread(url = get(5).formattedFbUrl,
                        img = get(1).formattedFbUrl,
                        title = get(2),
                        content = get(3),
                        time = get(4).toLong(),
                        unread = !get(7).contains("acw"))
            }
        }.toList()

        val links: List<FrostLink> = matches.map { FrostRegex.LINK.matcher.findAll(it.groupValues[3]) }
                .flatten().map {
            with(it.groupValues) {
                FrostLink(text = get(2), href = get(1).formattedFbUrl)
            }
        }.toList()
        return Pair(threads, links)
    }

    override fun debugImpl(output: Pair<List<FrostThread>, List<FrostLink>>, result: MutableList<String>) {
        result.addAll(output.first.map { it.toString() })
        result.addAll(output.second.map { it.toString() })
    }
}
