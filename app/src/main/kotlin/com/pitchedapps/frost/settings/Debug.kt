package com.pitchedapps.frost.settings

import android.content.Context
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import com.afollestad.materialdialogs.MaterialDialog
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.facebook.*
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.cleanHtml
import com.pitchedapps.frost.utils.materialDialogThemed
import com.pitchedapps.frost.web.*
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup

/**
 * Created by Allan Wang on 2017-06-30.
 */
fun SettingsActivity.getDebugPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.experimental_disclaimer) {
        descRes = R.string.debug_disclaimer_info
    }

    Debugger.values().forEach {
        plainText(it.data.titleId) {
            onClick = { itemView, _, _ -> it.debug(itemView.context); true }
        }
    }

}

private enum class Debugger(val data: FbTab, val injector: InjectorContract?, vararg val query: String) {
    NOTIFICATIONS(FbTab.NOTIFICATIONS, null, "#notifications_list", "#root"),
    SEARCH(FbTab.SEARCH, null);

    fun debug(context: Context) {
        val dialog = context.materialDialogThemed {
            title("Debugging")
            progress(true, 0)
            canceledOnTouchOutside(false)
            positiveText(R.string.kau_cancel)
            onPositive { dialog, _ -> dialog.cancel() }
        }
        dialog.doAsync {
            if (injector != null) extractHtml(injector)
            else loadJsoup(query)
        }
    }

    /**
     * Wait for html to be returned from headless webview
     *
     * from [debug] to [createReport]
     */
    private fun AnkoAsyncContext<MaterialDialog>.extractHtml(injector: InjectorContract) {
        val md = weakRef.get() ?: return
        md.setContent("Fetching webpage")
        var disposable: Disposable? = null
        md.setOnCancelListener { disposable?.dispose() }
        md.context.launchHeadlessHtmlExtractor(data.url, injector) {
            disposable = it.subscribe {
                (html, flag) ->
                when (flag) {
                    HTML_EXTRACTOR_SUCCESS -> {
                        L.d("HHE HTML_EXTRACTOR_SUCCESS $html")
                        createReport(html)
                    }
                    HTML_EXTRACTOR_CANCELLED -> L.d("HHE HTML_EXTRACTOR_CANCELLED")
                    HTML_EXTRACTOR_ERROR -> {
                        L.d("HHE HTML_EXTRACTOR_ERROR")
                        errorDialog("HTML_EXTRACTOR_ERROR")
                    }
                    HTML_EXTRACTOR_TIMEOUT -> {
                        L.d("HHE HTML_EXTRACTOR_TIMEOUT")
                        errorDialog("HTML_EXTRACTOR_TIMEOUT")
                    }
                }
            }
        }
    }

    private fun AnkoAsyncContext<MaterialDialog>.errorDialog(content: String) {
        val md = weakRef.get() ?: return
        val c = md.context
        md.dismiss()
        c.materialDialogThemed {
            title(R.string.kau_error)
            content(content)
        }
    }

    /**
     * Get data directly from the link and search for our queries, returning the outerHTML
     * of the first query found
     *
     * from [debug] to [createReport]
     */
    private fun AnkoAsyncContext<MaterialDialog>.loadJsoup(query: Array<out String>) {
        val md = weakRef.get() ?: return
        md.setContent("Load Jsoup")
        md.setOnCancelListener(null)
        val connection = Jsoup.connect(data.url).cookie(FACEBOOK_COM, FbCookie.webCookie).userAgent(USER_AGENT_BASIC)
        val doc = connection.get()
        val q = query.firstOrNull { doc.select(it).isNotEmpty() } ?: "body"
        createReport(doc.select(q).outerHtml())
    }

    private fun AnkoAsyncContext<MaterialDialog>.createReport(html: String) {
        val md = weakRef.get() ?: return
        uiThread { md.setContent("Create Report") }
        md.context.sendEmail(R.string.dev_email, R.string.kau_cancel) {
            footer = html.cleanHtml()
            uiThread { md.dismiss() }
        }
    }
}