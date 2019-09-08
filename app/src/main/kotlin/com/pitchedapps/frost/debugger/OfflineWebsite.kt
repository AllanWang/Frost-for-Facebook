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

import ca.allanwang.kau.logging.KauLoggerExtension
import ca.allanwang.kau.utils.copyFromInputStream
import com.pitchedapps.frost.facebook.FB_CSS_URL_MATCHER
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.utils.createFreshDir
import com.pitchedapps.frost.utils.createFreshFile
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.utils.unescapeHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.HttpUrl
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by Allan Wang on 04/01/18.
 *
 * Helper to download html files and assets for offline viewing
 *
 * Inspired by <a href="https://github.com/JonasCz/save-for-offline">Save for Offline</a>
 */
class OfflineWebsite(
    private val url: String,
    private val cookie: String = "",
    baseUrl: String? = null,
    private val html: String? = null,
    /**
     * Directory that holds all the files
     */
    val baseDir: File,
    private val userAgent: String = USER_AGENT
) {

    /**
     * Supplied url without the queries
     */
    private val baseUrl: String = baseUrl ?: run {
        val url: HttpUrl = HttpUrl.parse(url) ?: throw IllegalArgumentException("Malformed url")
        return@run "${url.scheme()}://${url.host()}"
    }

    private val mainFile = File(baseDir, "index.html")
    private val assetDir = File(baseDir, "assets")

    private val urlMapper = ConcurrentHashMap<String, String>()
    private val atomicInt = AtomicInteger()

    private val L = KauLoggerExtension("Offline", com.pitchedapps.frost.utils.L)

    init {
        if (!this.baseUrl.startsWith("http"))
            throw IllegalArgumentException("Base Url must start with http")
    }

    private val fileQueue = mutableSetOf<String>()

    private val cssQueue = mutableSetOf<String>()

    private fun request(url: String) = Request.Builder()
        .header("Cookie", cookie)
        .header("User-Agent", userAgent)
        .url(url)
        .get()
        .call()

    /**
     * Caller to bind callbacks and start the load
     * Callback is guaranteed to be called unless the load is cancelled
     */
    suspend fun load(progress: (Int) -> Unit = {}): Boolean = withContext(Dispatchers.IO) {
        reset()

        L.v { "Saving $url to ${baseDir.absolutePath}" }

        if (!baseDir.isDirectory && !baseDir.mkdirs()) {
            L.e { "Could not make directory" }
            return@withContext false
        }

        if (!mainFile.createNewFile()) {
            L.e { "Could not create ${mainFile.absolutePath}" }
            return@withContext false
        }

        if (!assetDir.createFreshDir()) {
            L.e { "Could not create ${assetDir.absolutePath}" }
            return@withContext false
        }

        progress(10)

        yield()

        val doc: Document
        if (html == null || html.length < 100) {
            doc = frostJsoup(cookie, url)
        } else {
            doc = Jsoup.parse("<html>${html.unescapeHtml()}</html>")
            L.d { "Building data from supplied content of size ${html.length}" }
        }
        doc.setBaseUri(baseUrl)
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended)
        if (doc.childNodeSize() == 0) {
            L.e { "No content found" }
            return@withContext false
        }

        yield()

        progress(35)

        doc.collect("link[href][rel=stylesheet]", "href", cssQueue)
        doc.collect("link[href]:not([rel=stylesheet])", "href", fileQueue)
        doc.collect("img[src]", "src", fileQueue)
        doc.collect("img[data-canonical-src]", "data-canonical-src", fileQueue)
        doc.collect("script[src]", "src", fileQueue)

        // make links absolute
        doc.select("a[href]").forEach {
            val absLink = it.attr("abs:href")
            it.attr("href", absLink)
        }

        yield()

        mainFile.writeText(doc.html())

        progress(50)

        fun partialProgress(from: Int, to: Int, steps: Int): (Int) -> Unit {
            if (steps == 0) return { progress(to) }
            val section = (to - from) / steps
            return { progress(from + it * section) }
        }

        val cssProgress = partialProgress(50, 70, cssQueue.size)

        cssQueue.clean().forEachIndexed { index, url ->
            yield()
            cssProgress(index)
            val newUrls = downloadCss(url)
            fileQueue.addAll(newUrls)
        }

        progress(70)

        val fileProgress = partialProgress(70, 100, fileQueue.size)

        fileQueue.clean().forEachIndexed { index, url ->
            yield()
            fileProgress(index)
            if (!downloadFile(url))
                return@withContext false
        }

        yield()
        progress(100)
        return@withContext true
    }

    fun zip(name: String): Boolean {
        try {
            val zip = File(baseDir, "$name.zip")
            if (!zip.createFreshFile()) {
                L.e { "Failed to create zip at ${zip.absolutePath}" }
                return false
            }

            ZipOutputStream(FileOutputStream(zip)).use { out ->

                fun File.zip(name: String = this.name) {
                    if (!isFile) return
                    inputStream().use { file ->
                        out.putNextEntry(ZipEntry(name))
                        file.copyTo(out)
                    }
                    out.closeEntry()
                    delete()
                }
                baseDir.listFiles { file -> file != zip }
                    ?.forEach { it.zip() }
                assetDir.listFiles()
                    ?.forEach { it.zip("assets/${it.name}") }

                assetDir.delete()
            }
            return true
        } catch (e: Exception) {
            L.e { "Zip failed: ${e.message}" }
            return false
        }
    }

    suspend fun loadAndZip(name: String, progress: (Int) -> Unit = {}): Boolean =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val success = load { progress((it * 0.85f).toInt()) }
                if (!success) return@coroutineScope false
                val result = zip(name)
                progress(100)
                return@coroutineScope result
            }
        }

    private fun downloadFile(url: String): Boolean {
        return try {
            val file = File(assetDir, fileName(url))
            file.createNewFile()
            val stream = request(url).execute().body()?.byteStream()
                ?: throw IllegalArgumentException("Response body not found for $url")
            file.copyFromInputStream(stream)
            true
        } catch (e: Exception) {
            L.e(e) { "Download file failed" }
            false
        }
    }

    private fun downloadCss(url: String): Set<String> {
        return try {
            val file = File(assetDir, fileName(url))
            file.createNewFile()

            var content = request(url).execute().body()?.string()
                ?: throw IllegalArgumentException("Response body not found for $url")
            val links = FB_CSS_URL_MATCHER.findAll(content).mapNotNull { it[1] }
            val absLinks = links.mapNotNull {
                val newUrl = when {
                    it.startsWith("http") -> it
                    it.startsWith("/") -> "$baseUrl$it"
                    else -> return@mapNotNull null
                }
                // css files are already in the asset folder,
                // so the url does not point to another subfolder
                content = content.replace(it, fileName(newUrl))
                newUrl
            }.toSet()

            file.writeText(content)
            absLinks
        } catch (e: Exception) {
            L.e(e) { "Download css failed" }
            emptySet()
        }
    }

    private fun Element.collect(query: String, key: String, collector: MutableSet<String>) {
        val data = select(query)
        L.v { "Found ${data.size} elements with $query" }
        data.forEach {
            val absLink = it.attr("abs:$key")
            if (!absLink.isValid) return@forEach
            collector.add(absLink)
            it.attr(key, "assets/${fileName(absLink)}")
        }
    }

    private inline val String.isValid
        get() = startsWith("http")

    /**
     * Fetch the previously discovered filename
     * or create a new one
     * This is thread-safe
     */
    private fun fileName(url: String): String {
        val mapped = urlMapper[url]
        if (mapped != null) return mapped

        val candidate = url.substringBefore("?").trim('/')
            .substringAfterLast("/").shorten()

        val index = atomicInt.getAndIncrement()

        var newUrl = "a${index}_$candidate"

        /**
         * This is primarily for zipping up and sending via emails
         * As .js files typically aren't allowed, we'll simply make everything txt files
         */
        if (newUrl.endsWith(".js"))
            newUrl = "$newUrl.txt"

        urlMapper[url] = newUrl
        return newUrl
    }

    private fun String.shorten() =
        if (length <= 10) this else substring(length - 10)

    private fun Set<String>.clean(): List<String> =
        filter(String::isNotBlank).filter { it.startsWith("http") }

    private fun reset() {
        urlMapper.clear()
        atomicInt.set(0)
        fileQueue.clear()
        cssQueue.clear()
    }
}
