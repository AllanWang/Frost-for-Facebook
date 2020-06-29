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
package com.pitchedapps.frost.facebook.parsers

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.assertComponentsNotEmpty
import com.pitchedapps.frost.internal.assertDescending
import com.pitchedapps.frost.internal.authDependent
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.BeforeClass
import org.junit.Test

/**
 * Created by Allan Wang on 24/12/17.
 */
class FbParseTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            authDependent()
        }
    }

    private inline fun <reified T : ParseData> FrostParser<T>.test(action: T.() -> Unit = {}) =
        parse(COOKIE).test(url, action)

    private inline fun <reified T : ParseData> ParseResponse<T>?.test(
        url: String,
        action: T.() -> Unit = {}
    ) {
        val response = this
            ?: fail("${T::class.simpleName} parser returned null for $url")
        println(response)
        assertFalse(
            response.data.isEmpty,
            "${T::class.simpleName} parser returned empty data for $url"
        )
        response.data.action()
    }

    @Test
    fun message() = MessageParser.test {
        threads.forEach {
            it.assertComponentsNotEmpty()
            assertTrue(it.id > FALLBACK_TIME_MOD, "id may not be properly matched")
            assertNotNull(it.img, "img may not be properly matched")
        }
        threads.map(FrostThread::time).assertDescending("thread time values")
    }

    @Test
    fun messageUser() = MessageParser.queryUser(COOKIE, "allan").test("allan query")

    @Test
    fun search() = SearchParser.test()

    @Test
    fun notif() = NotifParser.test {
        notifs.forEach {
            it.assertComponentsNotEmpty()
            assertTrue(it.id > FALLBACK_TIME_MOD, "id may not be properly matched")
            assertNotNull(it.img, "img may not be properly matched")
        }
        notifs.map(FrostNotif::time).assertDescending("notif time values")
        if (notifs.none { it.unread }) {
            println("No messages unread.")
        }
        notifs.forEach {
            assertFalse(
                it.content.startsWith("unread", ignoreCase = true),
                "Parse error; notif starts with 'Unread'"
            )
        }
    }

    @Test
    fun badge() = BadgeParser.test()
}
