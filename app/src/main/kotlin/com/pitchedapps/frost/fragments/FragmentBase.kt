package com.pitchedapps.frost.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.withArguments
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.contracts.ActivityContract
import com.pitchedapps.frost.contracts.DynamicUiContract
import com.pitchedapps.frost.contracts.FrostViewContract
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_REFRESH
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
import com.pitchedapps.frost.views.FrostRefreshView
import com.pitchedapps.frost.views.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewClient
import com.pitchedapps.frost.web.FrostWebViewClientMenu
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-11-07.
 */
abstract class BaseFragment : Fragment(), FragmentContract, DynamicUiContract {

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

    override val url: String by lazy { arguments!!.getString(ARG_URL) }
    override val urlEnum: FbItem by lazy { arguments!!.getSerializable(ARG_URL_ENUM) as FbItem }
    override val position: Int by lazy { arguments!!.getInt(ARG_POSITION) }

    override var firstLoad: Boolean = true
    private var activityDisposable: Disposable? = null
    private var onCreateRunnable: ((FragmentContract) -> Unit)? = null

    protected var refreshView: FrostRefreshView? = null
    protected val delegate
        get() = refreshView?.inner

    override final fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val refresh = FrostRefreshView(context!!)
        this.refreshView = refresh
        refresh.baseUrl = url
        refresh.baseEnum = urlEnum
        val viewContract = innerView(context!!)
        viewContract.bind(refresh)
        return refresh
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            delegate?.reload(true)
            firstLoad = false
        }
    }

    override fun post(action: (fragment: FragmentContract) -> Unit) {
        onCreateRunnable = action
    }

    override fun attachMainObservable(contract: ActivityContract): Disposable =
            contract.fragmentSubject.observeOn(AndroidSchedulers.mainThread()).subscribe {
                when (it) {
                    REQUEST_REFRESH -> {
                        delegate?.apply {
                            reload(true)
                            clearHistory()
                        }
                    }
                    position -> {
                        contract.setTitle(urlEnum.titleId)
                        delegate?.onScrollTo()
                    }
                    -(position + 1) -> {
                        delegate?.onScrollFrom()
                    }
                    REQUEST_TEXT_ZOOM -> {
                        delegate?.reloadTextSize()
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

    override fun onDestroyView() {
        delegate?.destroy()
        refreshView = null
        super.onDestroyView()
    }

    override fun onBackPressed() = delegate?.onBackPressed() ?: false

    override fun reloadTextSize() {
        refreshView?.reloadTextSize()
    }

    override fun reloadTheme() {
        refreshView?.reloadTextSize()
    }

    override fun onTabClick() {
        delegate?.scrollOrRefresh()
    }
}

abstract class RecyclerFragment<T> : BaseFragment(), NativeFragmentContract<T> {


    override fun revertToWeb() {
        //todo
    }

}

open class WebFragment : BaseFragment(), FragmentContract {

    /**
     * Given a webview, output a client
     */
    open fun client(web: FrostWebView) = FrostWebViewClient(web)

    override fun innerView(context: Context) = FrostWebView(context)

}

class WebFragmentMenu : WebFragment() {

    override fun client(web: FrostWebView) = FrostWebViewClientMenu(web)

}