package com.pitchedapps.frost.internal

import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.RequestAuth
import com.pitchedapps.frost.facebook.requests.getAuth
import com.pitchedapps.frost.utils.frostJsoup
import io.reactivex.Completable
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.starProjectedType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Created by Allan Wang on 21/12/17.
 */

private const val FILE = "priv.properties"

val PROPS: Properties by lazy {
    val props = Properties()
    val file = File(FILE)
    if (!file.exists()) {
        println("$FILE not found")
        return@lazy props
    }
    println("Found properties at ${file.absolutePath}")
    FileInputStream(file).use { props.load(it) }
    props
}

val COOKIE: String by lazy { PROPS.getProperty("COOKIE") ?: "" }
val USER_ID: Long by lazy { FB_USER_MATCHER.find(COOKIE)[1]?.toLong() ?: -1 }
val AUTH: RequestAuth by lazy {
    COOKIE.getAuth().apply {
        println("Auth:\nuser:$userId\nfb_dtsg: $fb_dtsg\nrev: $rev\nvalid: $isValid")
    }
}

val VALID_COOKIE: Boolean by lazy {
    val data = testJsoup(FbItem.SETTINGS.url)
    data.title() == "Settings"
}

fun testJsoup(url: String) = frostJsoup(COOKIE, url)

fun authDependent() {
    println("Auth Dependent")
    Assume.assumeTrue(COOKIE.isNotEmpty() && VALID_COOKIE)
    Assume.assumeTrue(AUTH.isValid)
}

/**
 * Check that component strings are nonempty and are properly parsed
 * To be used for data classes
 */
fun Any.assertComponentsNotEmpty() {
    val components = this::class.members.filter { it.name.startsWith("component") }
    if (components.isEmpty())
        fail("${this::class.simpleName} has no components")
    components.forEach {
        when (it.returnType) {
            String::class.starProjectedType -> {
                val result = it.call(this) as String
                assertTrue(result.isNotEmpty(), "${it.name} returned empty string")
                if (result.startsWith("https"))
                    assertTrue(result.startsWith("https://"), "${it.name} has poorly formatted output $result")
            }
        }
    }
}

fun <T : Comparable<T>> List<T>.assertDescending(tag: String) {
    assertEquals(sortedDescending(), this, "$tag not sorted in descending order")
}

interface CompletableCallback {
    fun onComplete()
    fun onError(message: String)
}

inline fun concurrentTest(crossinline caller: (callback: CompletableCallback) -> Unit) {
    val result = Completable.create { emitter ->
        caller(object : CompletableCallback {
            override fun onComplete() = emitter.onComplete()
            override fun onError(message: String) = emitter.onError(Throwable(message))
        })
    }.blockingGet(5, TimeUnit.SECONDS)
    if (result != null)
        throw RuntimeException("Concurrent fail: ${result.message}")
}

class InternalTest {
    @Test
    fun concurrentTest() = try {
        concurrentTest { result ->
            Thread().run {
                Thread.sleep(100)
                result.onError("Intentional fail")
            }
        }
        fail("Did not throw exception")
    } catch (e: Exception) {
        // pass
    }
}