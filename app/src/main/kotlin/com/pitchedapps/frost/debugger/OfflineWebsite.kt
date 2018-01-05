package com.pitchedapps.frost.debugger

import ca.allanwang.kau.logging.KauLoggerExtension
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.facebook.requests.zip
import com.pitchedapps.frost.utils.frostJsoup
import io.reactivex.disposables.Disposable
import okhttp3.Request
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.select.Elements
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Allan Wang on 04/01/18.
 */
class OfflineWebsite(private val url: String,
                     private val cookie: String = "",
                     baseDirectory: String) {

    private val baseUrl = url.substringBefore("?")

    private val baseDir = File(baseDirectory)
    private val mainFile = File(baseDir, "index.html")
    private val assetDir = File(baseDir, "assets")

    private val urlMapper = ConcurrentHashMap<String, String>()
    private val atomicInt = AtomicInteger()

    private val disposables = mutableListOf<Disposable>()

    init {
        if (!baseUrl.startsWith("http"))
            throw IllegalArgumentException("Base Url must start with http")
    }

    companion object {
        private val L = KauLoggerExtension("Offline", com.pitchedapps.frost.utils.L)
    }

    var userAgent = USER_AGENT_BASIC

    private val fileQueue = mutableSetOf<String>()

    private val cssQueue = mutableSetOf<String>()

    private fun request(url: String) = Request.Builder()
            .header("Cookie", cookie)
            .header("User-Agent", userAgent)
            .url(url)
            .get()
            .call()

    fun load(progress: (Int) -> Unit = {}, callback: (Boolean) -> Unit) {
        reset()

        L.v { "Saving $url to ${baseDir.absolutePath}" }
        if (baseDir.exists() && !baseDir.deleteRecursively()) {
            L.e { "Could not clean directory" }
            return callback(false)
        }

        if (!baseDir.mkdirs()) {
            L.e { "Could not make directory" }
            return callback(false)
        }


        if (!mainFile.createNewFile()) {
            L.e { "Could not create ${mainFile.absolutePath}" }
            return callback(false)
        }


        if (!assetDir.mkdirs()) {
            L.e { "Could not create ${assetDir.absolutePath}" }
            return callback(false)
        }

        val doc = frostJsoup(cookie, url)
        doc.setBaseUri(baseUrl)
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended)
        if (doc.childNodeSize() == 0) {
            L.e { "No content found" }
            return callback(false)
        }

        doc.collect("link[href][rel=stylesheet]", "href", cssQueue)
        doc.collect("link[href]:not([rel=stylesheet])", "href", fileQueue)
        doc.collect("img[src]", "src", fileQueue)
        doc.collect("img[data-canonical-src]", "data-canonical-src", fileQueue)
        doc.collect("script[src]", "src", fileQueue)

        println(fileQueue.clean())
        println(cssQueue.clean())

        // make links absolute
        doc.select("a[href]").forEach {
            val absLink = it.attr("abs:href")
            it.attr("href", absLink)
        }

        mainFile.writeText(doc.html())

        val fileDownloads = fileQueue.downloadUrls().subscribe { success, throwable ->
            L.v { "All files downloaded: $success with throwable $throwable" }
            callback(true)
        }

        disposables.add(fileDownloads)
    }

    private fun Set<String>.downloadUrls() = clean().toTypedArray().zip<String, Boolean, Boolean>({
        it.all { it }
    }, {
        val file = File(assetDir, it.fileName())
        if (!file.createNewFile()) {
            L.e { "Could not create path for ${file.absolutePath}" }
            return@zip false
        }
        var success = false
        request(url).execute().body()?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
                success = true
            }
        }
        return@zip success
    })

    private fun Element.collect(query: String, key: String, collector: MutableSet<String>) {
        val data = select(query)
        L.v { "Found ${data.size} elements with $query" }
        data.forEach {
            val absLink = it.attr("abs:$key")
            if (!absLink.isValid) return@forEach
            collector.add(absLink)
            it.attr(key, absLink.fileName())
        }
    }

    private val String.isValid
        get() = startsWith("http")

    /**
     * Fetch the previously discovered filename
     * or create a new one
     * This is thread-safe
     */
    private fun String.fileName(): String {
        val mapped = urlMapper[this]
        if (mapped != null) return mapped

        val candidate = substringBefore("?").trim('/')
                .substringAfterLast("/").shorten()

        val index = atomicInt.getAndIncrement()
        val newUrl = "a${index}_$candidate"
        urlMapper.put(this, newUrl)
        return newUrl
    }

    private fun String.shorten() =
            if (length <= 10) this else substring(length - 10)


    private fun Elements.updateAttr(key: String) =
            forEach {
                val orig = attr("abs:$key")
                attr(key, orig.fileName())
            }

    private fun Set<String>.clean()
            = filter(String::isNotBlank).filter { it.startsWith("http") }

    private fun String?.addTo(queue: MutableSet<String>) {
        if (this?.isNotBlank() != true)
            return
//        if (this.isNullOrBlank())
//            return
        var url = this
        if (startsWith("/"))
            url = "$baseUrl$this"
        if (!url.startsWith("http"))
            return
        queue.add(url)
    }

    private fun reset() {
        cancel()
        fileQueue.clear()
        cssQueue.clear()
    }

    private fun cancel() {
        disposables.forEach(Disposable::dispose)
        disposables.clear()
    }

}