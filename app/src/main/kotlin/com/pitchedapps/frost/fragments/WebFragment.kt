package com.pitchedapps.frost.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.withBundle
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.FeedSort
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.web.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewCore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-05-29.
 */


class WebFragment : Fragment() {

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_URL_ENUM = "arg_url_enum"
        private const val ARG_POSITION = "arg_position"

        operator fun invoke(data: FbTab, position: Int) = WebFragment().withBundle {
            putString(ARG_URL, data.url)
            putInt(ARG_POSITION, position)
            putSerializable(ARG_URL_ENUM, when (data) {
            //If is feed, check if sorting method is specified
                FbTab.FEED -> when (FeedSort(Prefs.feedSort)) {
                    FeedSort.DEFAULT -> data
                    FeedSort.MOST_RECENT -> FbTab.FEED_MOST_RECENT
                    FeedSort.TOP -> FbTab.FEED_TOP_STORIES
                }
                else -> data
            })
        }
    }

    //    val refresh: SwipeRefreshLayout by lazy { frostWebView.refresh }
    val web: FrostWebViewCore by lazy { frostWebView.web }
    val url: String by lazy { arguments.getString(ARG_URL) }
    val urlEnum: FbTab by lazy { arguments.getSerializable(ARG_URL_ENUM) as FbTab }
    val position: Int by lazy { arguments.getInt(ARG_POSITION) }
    lateinit var frostWebView: FrostWebView
    private var firstLoad = true
    private var activityDisposable: Disposable? = null
    private var onCreateRunnable: ((fragment: WebFragment) -> Unit)? = null

    /**
     * Hook to run action once fragment is properly created
     * This is not saved elsewhere and may not always execute
     */
    fun post(action: (fragment: WebFragment) -> Unit) {
        onCreateRunnable = action
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        frostWebView = FrostWebView(context)
        frostWebView.web.setupWebview(url, urlEnum)
        return frostWebView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateRunnable?.invoke(this)
        onCreateRunnable = null
        firstLoad()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        firstLoad()
    }

    fun firstLoad() {
        if (userVisibleHint && isVisible && firstLoad) {
            web.loadBaseUrl()
            firstLoad = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityDisposable?.dispose()
        if (context is MainActivity) {
            activityDisposable = context.webFragmentObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                /**
                 * Execute actions based on flags
                 * Flags between -10 and 10 are reserved for viewpager events
                 */
                when (it) {
                    MainActivity.FRAGMENT_REFRESH -> {
                        web.clearHistory()
                        web.loadBaseUrl(true)
                    }
                    position -> {
                        context.toolbar.setTitle(urlEnum.titleId)
                        pauseLoad = false
                    }
                    -(position + 1) -> { //we are moving away from this fragment
                        if (!frostWebView.refresh.isRefreshing) pauseLoad = true
                    }
                }
            }
        }
    }

    override fun onDetach() {
        activityDisposable?.dispose()
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        pauseLoad = false
        firstLoad()
    }

    var pauseLoad: Boolean
        get() = web.settings.blockNetworkLoads
        set(value) {
            web.settings.blockNetworkLoads = value
        }


    fun onBackPressed() = frostWebView.onBackPressed()
}