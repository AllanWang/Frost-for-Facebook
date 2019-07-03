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
package com.pitchedapps.frost.debugger

import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.internal.COOKIE
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assume.assumeTrue
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 05/01/18.
 */
class OfflineWebsiteTest {

    lateinit var server: MockWebServer
    lateinit var baseDir: File

    @BeforeTest
    fun before() {
        val buildPath = if (File("").absoluteFile.name == "app") "build/offline_test" else "app/build/offline_test"
        baseDir = File(buildPath)
        assertTrue(baseDir.deleteRecursively(), "Failed to clean base dir")
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun after() {
        server.shutdown()
    }

    private fun zipAndFetch(url: String = server.url("/").toString(), cookie: String = ""): ZipFile {
        val name = "test${System.currentTimeMillis()}"
        runBlocking {
            val success = OfflineWebsite(url, cookie, baseDir = baseDir)
                .loadAndZip(name)
            assertTrue(success, "An error occurred")
        }

        return ZipFile(File(baseDir, "$name.zip"))
    }

    private val tagWhitespaceRegex = Regex(">\\s+<", setOf(RegexOption.MULTILINE))

    private fun ZipFile.assertContentEquals(path: String, content: String) {
        val entry = getEntry(path)
        assertNotNull(entry, "Entry $path not found")
        val actualContent = getInputStream(entry).bufferedReader().use { it.readText() }
        assertEquals(
            content.replace(tagWhitespaceRegex, "><").toLowerCase(),
            actualContent.replace(tagWhitespaceRegex, "><").toLowerCase(), "Content mismatch for $path"
        )
    }

    @Ignore("Not really a test")
    @Test
    fun fbOffline() {
        // Not really a test. Skip in CI
        assumeTrue(COOKIE.isNotEmpty())
        zipAndFetch(FB_URL_BASE)
    }

    @Test
    fun basicSingleFile() {
        val content = """
            <!DOCTYPE html>
            <html>
                <head></head>
                <body>
                    <h1>Single File Test</h1>
                </body>
            </html>
        """.trimIndent()

        server.enqueue(MockResponse().setBody(content))

        val zip = zipAndFetch()

        assertEquals(1, zip.size(), "1 file expected")
        zip.assertContentEquals("index.html", content)
    }

    @Test
    fun withCssAsset() {
        val cssUrl = server.url("1.css")

        val content = """
            <!DOCTYPE html>
            <html>
                <head>
                    <link rel="stylesheet" href="$cssUrl">
                </head>
                <body>
                    <h1>Css File Test</h1>
                </body>
            </html>
        """.trimIndent()

        val css1 = """
            .hello {
                display: none;
            }
        """.trimIndent()

        server.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when {
                    request.path.contains(cssUrl.encodedPath()) -> MockResponse().setBody(css1)
                    else -> MockResponse().setBody(content)
                }
        })

        val zip = zipAndFetch()

        assertEquals(2, zip.size(), "2 files expected")
        zip.assertContentEquals("index.html", content.replace(cssUrl.toString(), "assets/a0_1.css"))
        zip.assertContentEquals("assets/a0_1.css", css1)
    }

    @Test
    fun withJsAsset() {
        val jsUrl = server.url("1.js")

        val content = """
            <!DOCTYPE html>
            <html>
                <head></head>
                <body>
                    <h1>Js File Test</h1>
                    <script type="text/javascript" src="$jsUrl"></script>
                </body>
            </html>
        """.trimIndent()

        val js1 = """
            console.log('hello');
        """.trimIndent()

        server.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when {
                    request.path.contains(jsUrl.encodedPath()) -> MockResponse().setBody(js1)
                    else -> MockResponse().setBody(content)
                }
        })

        val zip = zipAndFetch()

        assertEquals(2, zip.size(), "2 files expected")
        zip.assertContentEquals("index.html", content.replace(jsUrl.toString(), "assets/a0_1.js.txt"))
        zip.assertContentEquals("assets/a0_1.js.txt", js1)
    }

    @Test
    fun fullTest() {
        val css1Url = server.url("1.css")
        val css2Url = server.url("2.css")
        val js1Url = server.url("1.js")
        val js2Url = server.url("2.js")

        val content = """
            <!DOCTYPE html>
            <html>
                <head>
                    <link rel="stylesheet" href="$css1Url">
                    <link rel="stylesheet" href="$css2Url">
                </head>
                <body>
                    <h1>Multi File Test</h1>
                    <script type="text/javascript" src="$js1Url"></script>
                    <script type="text/javascript" src="$js2Url"></script>
                </body>
            </html>
        """.trimIndent()

        val css1 = """
            .hello {
                display: none;
            }
        """.trimIndent()

        val css2 = """
            .world {
                display: none;
            }
        """.trimIndent()

        val js1 = """
            console.log('hello');
        """.trimIndent()

        val js2 = """
            console.log('world');
        """.trimIndent()

        server.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when {
                    request.path.contains(css1Url.encodedPath()) -> MockResponse().setBody(css1)
                    request.path.contains(css2Url.encodedPath()) -> MockResponse().setBody(css2)
                    request.path.contains(js1Url.encodedPath()) -> MockResponse().setBody(js1)
                    request.path.contains(js2Url.encodedPath()) -> MockResponse().setBody(js2)
                    else -> MockResponse().setBody(content)
                }
        })

        val zip = zipAndFetch()

        assertEquals(5, zip.size(), "2 files expected")
        zip.assertContentEquals(
            "index.html", content
                .replace(css1Url.toString(), "assets/a0_1.css")
                .replace(css2Url.toString(), "assets/a1_2.css")
                .replace(js1Url.toString(), "assets/a2_1.js.txt")
                .replace(js2Url.toString(), "assets/a3_2.js.txt")
        )

        zip.assertContentEquals("assets/a0_1.css", css1)
        zip.assertContentEquals("assets/a1_2.css", css2)
        zip.assertContentEquals("assets/a2_1.js.txt", js1)
        zip.assertContentEquals("assets/a3_2.js.txt", js2)
    }
}
