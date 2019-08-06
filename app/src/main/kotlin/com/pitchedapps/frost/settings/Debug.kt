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
package com.pitchedapps.frost.settings

import android.content.Context
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.startActivityForResult
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.toast
import ca.allanwang.kau.utils.withMainContext
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.DebugActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.activities.SettingsActivity.Companion.ACTIVITY_REQUEST_DEBUG
import com.pitchedapps.frost.debugger.OfflineWebsite
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.parsers.FrostParser
import com.pitchedapps.frost.facebook.parsers.MessageParser
import com.pitchedapps.frost.facebook.parsers.NotifParser
import com.pitchedapps.frost.facebook.parsers.SearchParser
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostUriFromFile
import com.pitchedapps.frost.utils.sendFrostEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by Allan Wang on 2017-06-30.
 *
 * A sub pref section that is enabled through a hidden preference
 * Each category will load a page, extract the contents, remove private info, and create a report
 */
fun SettingsActivity.getDebugPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.disclaimer) {
        descRes = R.string.debug_disclaimer_info
    }

    plainText(R.string.debug_web) {
        descRes = R.string.debug_web_desc
        onClick =
            { this@getDebugPrefs.startActivityForResult<DebugActivity>(ACTIVITY_REQUEST_DEBUG) }
    }

    plainText(R.string.debug_parsers) {
        descRes = R.string.debug_parsers_desc
        onClick = {

            val parsers = arrayOf(NotifParser, MessageParser, SearchParser)

            materialDialog {
                listItems(items = parsers.map { string(it.nameRes) }) { dialog, position, _ ->
                    dialog.dismiss()
                    val parser = parsers[position]
                    var attempt: Job? = null
                    val loading = materialDialog {
                        message(parser.nameRes)
                        // TODO change dialog? No more progress view
                        negativeButton(R.string.kau_cancel) {
                            attempt?.cancel()
                            it.dismiss()
                        }
                        cancelOnTouchOutside(false)
                    }

                    attempt = launch(Dispatchers.IO) {
                        try {
                            val data = parser.parse(FbCookie.webCookie)
                            withMainContext {
                                loading.dismiss()
                                createEmail(parser, data?.data)
                            }
                        } catch (e: Exception) {
                            createEmail(parser, "Error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
}

private fun Context.createEmail(parser: FrostParser<*>, content: Any?) =
    sendFrostEmail("${string(R.string.debug_report)}: ${parser::class.java.simpleName}") {
        addItem("Url", parser.url)
        addItem("Contents", "$content")
    }

private const val ZIP_NAME = "debug"

fun SettingsActivity.sendDebug(url: String, html: String?) {

    val downloader = OfflineWebsite(
        url, FbCookie.webCookie ?: "",
        baseUrl = FB_URL_BASE,
        html = html,
        baseDir = DebugActivity.baseDir(this)
    )

    val job = Job()

    val md = materialDialog {
        title(R.string.parsing_data)
        // TODO remove dialog? No progress ui
        negativeButton(R.string.kau_cancel) { it.dismiss() }
        cancelOnTouchOutside(false)
        onDismiss { job.cancel() }
    }

    val progressChannel = Channel<Int>(10)

    launchMain {
        for (p in progressChannel) {
//            md.setProgress(p)
        }
    }

    launchMain {
        val success = downloader.loadAndZip(ZIP_NAME) {
            progressChannel.offer(it)
        }
        md.dismiss()
        progressChannel.close()
        if (success) {
            val zipUri = frostUriFromFile(
                File(downloader.baseDir, "$ZIP_NAME.zip")
            )
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
