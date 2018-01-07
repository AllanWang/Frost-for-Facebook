package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.startActivityForResult
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.DebugActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.activities.SettingsActivity.Companion.ACTIVITY_REQUEST_DEBUG
import com.pitchedapps.frost.debugger.OfflineWebsite
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostUriFromFile
import com.pitchedapps.frost.utils.sendFrostEmail
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File

/**
 * Created by Allan Wang on 2017-06-30.
 *
 * A sub pref section that is enabled through a hidden preference
 * Each category will load a page, extract the contents, remove private info, and create a report
 */
fun SettingsActivity.getDebugPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.experimental_disclaimer) {
        descRes = R.string.debug_disclaimer_info
    }

    plainText(R.string.debug_web) {
        descRes = R.string.debug_web_desc
        onClick = { this@getDebugPrefs.startActivityForResult<DebugActivity>(ACTIVITY_REQUEST_DEBUG) }
    }
}

private const val ZIP_NAME = "debug"

fun SettingsActivity.sendDebug(urlOrig: String) {

    val url = when {
        urlOrig.endsWith("soft=requests") -> FbItem.FRIENDS.url
        urlOrig.endsWith("soft=messages") -> FbItem.MESSAGES.url
        urlOrig.endsWith("soft=notifications") -> FbItem.NOTIFICATIONS.url
        urlOrig.endsWith("soft=search") -> "${FbItem._SEARCH.url}?q=a"
        else -> urlOrig
    }

    val downloader = OfflineWebsite(url, FbCookie.webCookie ?: "",
            File(externalCacheDir, "offline_debug"))

    val md = materialDialog {
        title(R.string.parsing_data)
        progress(false, 100)
        negativeText(R.string.kau_cancel)
        onNegative { dialog, _ -> dialog.dismiss() }
        canceledOnTouchOutside(false)
        dismissListener { downloader.cancel() }
    }

    md.doAsync {
        downloader.loadAndZip(ZIP_NAME, { progress ->
            uiThread { it.setProgress(progress) }
        }) { success ->
            uiThread {
                it.dismiss()
                if (success) {
                    val zipUri = it.context.frostUriFromFile(
                            File(downloader.baseDir, "$ZIP_NAME.zip"))
                    L.i { "Sending debug zip with uri $zipUri" }
                    sendFrostEmail(R.string.debug_report_email_title) {
                        addItem("Url", url)
                        addAttachment(zipUri)
                        extras = {
                            type = "application/zip"
                        }
                    }
                } else {
                    toast(R.string.error_generic)
                }
            }
        }

    }

}
