package com.pitchedapps.frost.settings

import android.content.Context
import android.support.annotation.UiThread
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.utils.string
import com.afollestad.materialdialogs.MaterialDialog
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.web.launchHeadlessHtmlExtractor
import com.pitchedapps.frost.web.query
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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

    Debugger.values().forEach {
        plainText(it.data.titleId) {
            iicon = it.data.icon
            onClick = {  it.debug(itemView.context) }
        }
    }

}

private enum class Debugger(val data: FbItem, val injector: InjectorContract?, vararg query: String) {
    MENU(FbItem.MENU, JsAssets.MENU_DEBUG, "#viewport"), //todo modify menu js for debugging
    NOTIFICATIONS(FbItem.NOTIFICATIONS, null, "#notifications_list");
//    SEARCH(FbItem.SEARCH, JsActions.FETCH_BODY);

    val query = if (query.isNotEmpty()) arrayOf(*query, "#root", "main", "body") else emptyArray()

    fun debug(context: Context) {
        val dialog = context.materialDialogThemed {
            title("Debugging")
            progress(true, 0)
            canceledOnTouchOutside(false)
            positiveText(R.string.kau_cancel)
            onPositive { dialog, _ -> dialog.cancel() }
        }
        if (injector != null) dialog.extractHtml(injector)
        else dialog.debugAsync {
            loadJsoup()
        }
    }

    fun MaterialDialog.debugAsync(task: AnkoAsyncContext<MaterialDialog>.() -> Unit) {
        doAsync({ t: Throwable ->
            val msg = t.message
            L.e("Debugger failed: $msg")
            context.runOnUiThread {
                cancel()
                context.materialDialogThemed {
                    title(R.string.debug_incomplete)
                    if (msg != null) content(msg)
                }
            }
        }, task)
    }

    /**
     * Wait for html to be returned from headless webview
     *
     * from [debug] to [simplifyJsoup] if [query] is not empty, or [createReport] otherwise
     */
    @UiThread
    private fun MaterialDialog.extractHtml(injector: InjectorContract) {
        setContent("Fetching webpage")
        var disposable: Disposable? = null
        setOnCancelListener { disposable?.dispose() }
        context.launchHeadlessHtmlExtractor(data.url, injector) {
            disposable = it.subscribe { (html, errorRes) ->
                debugAsync {
                    if (errorRes == -1) {
                        L.i("Debug report successful", html)
                        if (query.isNotEmpty()) simplifyJsoup(Jsoup.parseBodyFragment(html))
                        else createReport(html)
                    } else {
                        throw Throwable(context.string(errorRes))
                    }
                }
            }
        }
    }

    /**
     * Get data directly from the link and search for our queries, returning the outerHTML
     * of the first query found
     *
     * from [debug] to [simplifyJsoup]
     */
    private fun AnkoAsyncContext<MaterialDialog>.loadJsoup() {
        uiThread {
            it.setContent("Load Jsoup")
            it.setOnCancelListener(null)
            it.debugAsync { simplifyJsoup(frostJsoup(data.url)) }
        }
    }

    /**
     * Takes snippet of given document that matches the first query in the [query] items
     * before sending it to [createReport]
     */
    private fun AnkoAsyncContext<MaterialDialog>.simplifyJsoup(doc: Document) {
        weakRef.get() ?: return
        val q = query.first { doc.select(it).isNotEmpty() }
        createReport(doc.select(q).outerHtml())
    }

    private fun AnkoAsyncContext<MaterialDialog>.createReport(html: String) {
        val cleanHtml = html.cleanHtml()
        uiThread {
            val c = it.context
            it.dismiss()
            c.sendFrostEmail("${c.string(R.string.debug_report_email_title)} $name") {
                addItem("Query List", query.contentToString())
                footer = cleanHtml
            }
        }
    }
}