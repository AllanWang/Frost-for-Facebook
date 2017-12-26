package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.assertComponentsNotEmpty
import com.pitchedapps.frost.internal.assertDescending
import com.pitchedapps.frost.internal.authDependent
import com.pitchedapps.frost.parsers.*
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.fail

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

    private inline fun <T : Any> FrostParser<T>.test(action: T.() -> Unit = {}) {
        val response = parse(COOKIE)
                ?: fail("${this::class.java.simpleName} returned null for $url")
        println(response)
        response.data.action()
    }

    @Test
    fun message() = MessageParser.test {
        threads.forEach(FrostThread::assertComponentsNotEmpty)
        threads.map(FrostThread::time).assertDescending("thread time values")
    }

    @Test
    fun search() = SearchParser.test()

    @Test
    fun notif() = NotifParser.test {
        notifs.forEach(FrostNotif::assertComponentsNotEmpty)
        notifs.map(FrostNotif::time).assertDescending("notif time values")
    }
}