package com.pitchedapps.frost.internal

import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.utils.frostJsoup
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.util.*

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
val FB_DTSG: String by lazy { PROPS.getProperty("FB_DTSG") ?: "" }
val USER_ID: Long by lazy { FB_USER_MATCHER.find(COOKIE)[1]?.toLong() ?: -1 }

fun testJsoup(url: String) = frostJsoup(COOKIE, url)

class Internal {
    @Test
    fun test() {
        val data = testJsoup(FB_URL_BASE)
        println(data.html())
    }
}

