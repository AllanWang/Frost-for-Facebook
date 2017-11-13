package com.pitchedapps.frost.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.isAppEnabled
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.content.ActivityNotFoundException
import ca.allanwang.kau.utils.showAppInfo


/**
 * Created by Allan Wang on 2017-08-04.
 *
 * With reference to <a href="https://stackoverflow.com/questions/33434532/android-webview-download-files-like-browsers-do">Stack Overflow</a>
 */
fun Context.frostDownload(url: String?,
                          userAgent: String = USER_AGENT_BASIC,
                          contentDisposition: String? = null,
                          mimeType: String? = null,
                          contentLength: Long = 0L) {
    url ?: return
    frostDownload(Uri.parse(url), userAgent, contentDisposition, mimeType, contentLength)
}

fun Context.frostDownload(uri: Uri?,
                          userAgent: String = USER_AGENT_BASIC,
                          contentDisposition: String? = null,
                          mimeType: String? = null,
                          contentLength: Long = 0L) {
    uri ?: return
    L.d("Received download request", "Download $uri")
    if (uri.scheme != "http" && uri.scheme != "https")
        return L.e("Invalid download attempt", uri.toString())
    if (!isAppEnabled(DOWNLOAD_MANAGER_PACKAGE)) {
        materialDialogThemed {
            title(R.string.no_download_manager)
            content(R.string.no_download_manager_desc)
            positiveText(R.string.kau_yes)
            onPositive { _, _ -> showAppInfo(DOWNLOAD_MANAGER_PACKAGE) }
            negativeText(R.string.kau_no)
        }
        return
    }
    kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) { granted, _ ->
        if (!granted) return@kauRequestPermissions
        val request = DownloadManager.Request(uri)
        request.setMimeType(mimeType)
        val cookie = loadFbCookie(Prefs.userId) ?: return@kauRequestPermissions
        val title = URLUtil.guessFileName(uri.toString(), contentDisposition, mimeType)
        request.addRequestHeader("cookie", cookie.cookie)
        request.addRequestHeader("User-Agent", userAgent)
        request.setDescription(string(R.string.downloading))
        request.setTitle(title)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Frost/$title")
        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}

private const val DOWNLOAD_MANAGER_PACKAGE = "com.android.providers.downloads"