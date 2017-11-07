package com.pitchedapps.frost.fragments

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.withArguments
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.parsers.FrostParser
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.web.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewCore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-11-07.
 */
abstract class BaseFragment : Fragment(), FragmentContract {

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_URL_ENUM = "arg_url_enum"
        private const val ARG_POSITION = "arg_position"

        internal operator fun invoke(base: BaseFragment, data: FbItem, position: Int) = base.apply {
            val d = if (data == FbItem.FEED) FeedSort(Prefs.feedSort).item else data
            withArguments(
                    ARG_URL to d.url,
                    ARG_POSITION to position,
                    ARG_URL_ENUM to d
            )
        }
    }

    override val url: String by lazy { arguments.getString(ARG_URL) }
    override val urlEnum: FbItem by lazy { arguments.getSerializable(ARG_URL_ENUM) as FbItem }
    override val position: Int by lazy { arguments.getInt(ARG_POSITION) }

    override var firstLoad: Boolean = true
    private var activityDisposable: Disposable? = null
    private var onCreateRunnable: ((FragmentContract) -> Unit)? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateRunnable?.invoke(this)
        onCreateRunnable = null
        firstLoadRequest()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        firstLoadRequest()
    }

    override fun firstLoadRequest() {
        if (userVisibleHint && isVisible && firstLoad) {
            reload(true)
            firstLoad = false
        }
    }

    override fun post(action: (fragment: FragmentContract) -> Unit) {
        onCreateRunnable = action
    }

    override fun attachMainObservable(contract: ActivityContract): Disposable =
            contract.subject.observeOn(AndroidSchedulers.mainThread()).subscribe {
                when (it) {
                    FragmentRequest.REFRESH -> {
                        if (this is WebFragmentContract) reloadAndClear(true)
                        else reload(true)
                    }
                    position -> {
                        contract.setTitle(urlEnum.titleId)
                        onScrollTo()
                    }
                    -(position + 1) -> {
                        onScrollFrom()
                    }
                    FragmentRequest.TEXT_ZOOM -> {
                        reloadTextSize()
                    }
                }
            }

    override fun detachMainObservable() {
        activityDisposable?.dispose()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        detachMainObservable()
        if (context is ActivityContract)
            activityDisposable = attachMainObservable(context)
    }

    override fun onDetach() {
        detachMainObservable()
        super.onDetach()
    }

    @CallSuper
    override fun onScrollTo() = Unit

    @CallSuper
    override fun onScrollFrom() = Unit

}

class RecyclerFragment<T>(override val parser: FrostParser<T>) : BaseFragment(), NativeFragmentContract<T> {
    override fun reload(animate: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBackPressed() = false

    override fun reloadTextSize() {
    }

    override fun reloadTheme() {
    }

    override fun revertToWeb() {
        //todo
    }

}

class WebFragment2 : BaseFragment(), WebFragmentContract {

    companion object {
        operator fun invoke(data: FbItem, position: Int) =
                BaseFragment(WebFragment2(), data, position)
    }

    override lateinit var frostWebView: FrostWebView
    override val web: FrostWebViewCore by lazy { frostWebView.web }

    override var pauseLoad: Boolean
        get() = web.settings.blockNetworkLoads
        set(value) {
            web.settings.blockNetworkLoads = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        frostWebView = FrostWebView(context)
        frostWebView.setupWebview(url, urlEnum)
        return frostWebView
    }

    override fun reload(animate: Boolean) =
            web.reload(animate)

    override fun reloadTextSize() {
        web.settings.textZoom = Prefs.webTextScaling
    }

    override fun onScrollTo() {
        super.onScrollTo()
        pauseLoad = false
    }

    override fun onScrollFrom() {
        super.onScrollFrom()
        pauseLoad = true
    }

    override fun onBackPressed() =
            frostWebView.onBackPressed()

    override fun reloadAndClear(animate: Boolean) {
        web.clearHistory()
        reload(animate)
    }

    override fun reloadTheme() {
        reload(false) //todo just inject theme
    }
}