/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.utils

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-08-10.
 */
class JsoupCleanerTest {

    val whitespaceRegex = Regex("\\s+")

    fun String.cleanWhitespace() = replace("\n", "").replace(whitespaceRegex, " ").replace("> <", "><")

    private fun String.assertCleanHtml(expected: String) {
        assertEquals(expected.cleanWhitespace(), cleanHtml().cleanWhitespace())
    }

    private fun String.assertCleanJsoup(expected: String) {
        assertEquals(expected.cleanWhitespace(), cleanJsoup().cleanWhitespace())
    }

    private fun String.assertCleanText(expected: String) {
        assertEquals(expected.cleanWhitespace(), cleanText().cleanWhitespace())
    }

    @Test
    fun noChange() {
        "<a><aa> HI </aa></a>".assertCleanJsoup("<a><aa> HI </aa></a>")
    }

    @Test
    fun basicText() {
        """<div class="test">Hello world</div>""".assertCleanHtml("""<div class="test"></div>""")
    }

    @Test
    fun multiLineText() {
        """<div class="test">Hello
        world</div>""".assertCleanHtml("""<div class="test"></div>""")
    }

    @Test
    fun textRemoval() {
        """<div>Hello<a>World</a></div>""".assertCleanText("<div><a></a></div>")
    }

    @Test
    fun kau() {
        val html =
            """<div class="col s12 m6"> <div id="kau" class="card medium sticky-action"> <div class="card-image waves-effect waves-block waves-light"> <img class="activator" src="images/kau.jpg"> <span class="card-title activator background-gradient">KAU</span> </div><div class="card-content"><p>An extensive collection of Kotlin Android Utils</p></div><div class="card-action"> <a href="https://github.com/AllanWang/KAU" target="_blank" class="inline-block">Github</a> <a href="https://allanwang.github.io/KAU/" target="_blank" class="inline-block">Page</a> </div><div class="card-reveal"> <span class="card-title grey-text text-darken-4">KAU<i class="material-icons right">close</i></span> <ul class="browser-default"> <li>Huge package of one line extension functions</li><li>Custom UI views</li><li>Adapter items and animators</li><li>SearchView</li><li>Custom delegates</li></ul> </div></div></div>"""
        val expected =
            """<div class="col s12 m6"><div id="kau" class="card medium sticky-action"><div class="card-image waves-effect waves-block waves-light"><img class="activator" src="images/kau.jpg"></div><div class="card-action"><a href="-" target="_blank" class="inline-block"></a><a href="-" target="_blank" class="inline-block"></a></div></div></div>"""
        html.assertCleanHtml(expected)
    }
}
