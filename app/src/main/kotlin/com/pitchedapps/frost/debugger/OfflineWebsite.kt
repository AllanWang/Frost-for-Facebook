package com.pitchedapps.frost.debugger

import ca.allanwang.kau.logging.KauLoggerExtension
import com.pitchedapps.frost.facebook.FB_CSS_URL_MATCHER
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.facebook.requests.call
import com.pitchedapps.frost.facebook.requests.zip
import com.pitchedapps.frost.utils.frostJsoup
import io.reactivex.disposables.Disposable
import okhttp3.Request
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import java.io.BufferedOutputStream
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
class OfflineWebsite(private val url: String,
                     private val cookie: String = "",
                     baseDirectory: String,
                     private val userAgent: String = USER_AGENT_BASIC) {

    /**
     * Supplied url without the queries
     */
    val baseUrl = url.substringBefore("?").trim('/')

    /**
     * Directory that holds all the files
     */
    val baseDir = File(baseDirectory)

    private val mainFile = File(baseDir, "index.html")
    private val assetDir = File(baseDir, "assets")

    private val urlMapper = ConcurrentHashMap<String, String>()
    private val atomicInt = AtomicInteger()

    private val disposables = mutableListOf<Disposable>()

    private val L = KauLoggerExtension("Offline", com.pitchedapps.frost.utils.L)

    init {
        if (!baseUrl.startsWith("http"))
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
     */
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

        progress(10)

        val doc = frostJsoup(cookie, url)
        doc.setBaseUri(baseUrl)
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended)
        if (doc.childNodeSize() == 0) {
            L.e { "No content found" }
            return callback(false)
        }

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

        mainFile.writeText(doc.html())

        progress(50)

        downloadCss().subscribe { cssLinks, cssThrowable ->
            if (cssThrowable != null) {
                L.e { "CSS parsing failed" }
            }

            progress(70)

            fileQueue.addAll(cssLinks)

            val fileDownloads = downloadFiles().subscribe { success, throwable ->
                L.v { "All files downloaded: $success with throwable $throwable" }
                progress(100)
                callback(true)
            }

            disposables.add(fileDownloads)
        }
    }

    fun zip(zip: File): Boolean {
        L.d { "Zipping contents to ${zip.absolutePath}" }
        try {
            val parent = zip.parentFile
            if (!parent.exists() && !parent.mkdirs()) {
                L.e { "Failed to make folder at ${parent.absolutePath}" }
                return false
            }
            if (zip.exists() && (!zip.delete() || !zip.createNewFile())) {
                L.e { "Failed to create zip at ${zip.absolutePath}" }
                return false
            }

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zip))).use { out ->

                fun File.zip(name: String = this.name) = inputStream().use { file ->
                    out.putNextEntry(ZipEntry(name))
                    file.copyTo(out)
                }

                mainFile.zip()
                assetDir.listFiles().forEach {
                    it.zip("assets/${it.name}")
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun loadAndZip(zip: File, progress: (Int) -> Unit = {}, callback: (Boolean) -> Unit) {
        load({ progress((it * 0.85f).toInt()) }) {
            if (!it) callback(false)
            else {
                val result = zip(zip)
                progress(100)
                callback(result)
            }
        }

    }

    private fun downloadFiles() = fileQueue.clean().toTypedArray().zip<String, Boolean, Boolean>({
        it.all { it }
    }, {
        it.downloadUrl({ false }) { file, body ->
            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                    return@downloadUrl true
                }
            }
        }
    })

    private fun downloadCss() = cssQueue.clean().toTypedArray().zip<String, Set<String>, Set<String>>({
        it.flatMap { it }.toSet()
    }, {
        it.downloadUrl({ emptySet() }) { file, body ->
            var content = body.string()
            val links = FB_CSS_URL_MATCHER.findAll(content).mapNotNull { it[1] }
            val absLinks = links.mapNotNull {
                val url = when {
                    it.startsWith("http") -> it
                    it.startsWith("/") -> "$baseUrl$it"
                    else -> return@mapNotNull null
                }
                // css files are already in the asset folder,
                // so the url does not point to another subfolder
                content = content.replace(it, url.fileName())
                url
            }.toSet()

            L.v { "Abs links $absLinks" }

            file.writeText(content)
            return@downloadUrl absLinks
        }
    })

    private inline fun <T> String.downloadUrl(fallback: () -> T,
                                              action: (file: File, body: ResponseBody) -> T): T {

        val file = File(assetDir, fileName())
        if (!file.createNewFile()) {
            L.e { "Could not create path for ${file.absolutePath}" }
            return fallback()
        }

        val body = request(this).execute().body() ?: return fallback()

        try {
            body.use {
                return action(file, it)
            }
        } catch (e: Exception) {
            return fallback()
        }
    }

    private fun Element.collect(query: String, key: String, collector: MutableSet<String>) {
        val data = select(query)
        L.v { "Found ${data.size} elements with $query" }
        data.forEach {
            val absLink = it.attr("abs:$key")
            if (!absLink.isValid) return@forEach
            collector.add(absLink)
            it.attr(key, "assets/${absLink.fileName()}")
        }
    }

    private inline val String.isValid
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

    private fun Set<String>.clean()
            = filter(String::isNotBlank).filter { it.startsWith("http") }

    private fun reset() {
        cancel()
        fileQueue.clear()
        cssQueue.clear()
        baseDir.deleteRecursively()
    }

    private fun cancel() {
        disposables.forEach(Disposable::dispose)
        disposables.clear()
    }

}