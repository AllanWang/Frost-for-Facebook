package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.cookieDependent
import com.pitchedapps.frost.parsers.FrostMessages
import com.pitchedapps.frost.parsers.FrostParser
import com.pitchedapps.frost.parsers.MessageParser
import com.pitchedapps.frost.parsers.SearchParser
import com.raizlabs.android.dbflow.kotlinextensions.property
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
            cookieDependent()
        }
    }

    private fun <T : Any> FrostParser<T>.test(action: T.() -> Unit = {}) {
        val data = fromJsoup(COOKIE)
                ?: fail("${this::class.java.simpleName} returned null for $url")
        println(data)
        data.action()
    }

    @Test
    fun message() = MessageParser.test {
        threads.forEach {
//            for (prop in FrostMessages::class.members) {
//                println("${prop.name} = ${prop.get(user)}")
//            }
//            assert(it.img.isNotEmpty()) { "Empty img found" }
//            assert(it.url.isNotEmpty()) { "Empty url found" }
        }
    }

    @Test
    fun search() = SearchParser.test()
}