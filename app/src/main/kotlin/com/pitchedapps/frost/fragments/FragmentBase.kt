package com.pitchedapps.frost.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.withArguments
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.DynamicUiContract
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_REFRESH
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
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

    override val baseUrl: String by lazy { arguments!!.getString(ARG_URL) }
    override val baseEnum: FbItem? by lazy { arguments!!.getSerializable(ARG_URL_ENUM) as FbItem }
    override val position: Int by lazy { arguments!!.getInt(ARG_POSITION) }

    override var firstLoad: Boolean = true
    private var activityDisposable: Disposable? = null
    private var onCreateRunnable: ((FragmentContract) -> Unit)? = null

    override var content: FrostContentParent? = null

    protected abstract val layoutRes: Int

    override final fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutRes, container, false)
        val content = view  as? FrostContentParent
                ?: throw IllegalArgumentException("layoutRes for fragment must return view implementing FrostContentParent")
        this.content = content
        content.bind(this)
        return view
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
            core?.reloadBase(true)
            firstLoad = false
        }
    }

    override fun post(action: (fragment: FragmentContract) -> Unit) {
        onCreateRunnable = action
    }

    override fun setTitle(title: String) {
        // todo
    }

    override fun attachMainObservable(contract: MainActivityContract): Disposable =
            contract.fragmentSubject.observeOn(AndroidSchedulers.mainThread()).subscribe {
                when (it) {
                    REQUEST_REFRESH -> {
                        core?.apply {
                            reload(true)
                            clearHistory()
                        }
                    }
                    position -> {
                        contract.setTitle(baseEnum!!.titleId)
                        core?.active = true
                    }
                    -(position + 1) -> {
                        core?.active = false
                    }
                    REQUEST_TEXT_ZOOM -> {
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
        if (context is MainActivityContract)
            activityDisposable = attachMainObservable(context)
    }

    override fun onDetach() {
        detachMainObservable()
        super.onDetach()
    }

    override fun onDestroyView() {
        core?.destroy() // todo check
        content = null
        super.onDestroyView()
    }

    override fun reloadTheme() {
        reloadThemeSelf()
        content?.reloadTextSize()
    }

    override fun reloadThemeSelf() {
        // intentionally blank
    }

    override fun reloadTextSize() {
        reloadTextSizeSelf()
        content?.reloadTextSize()
    }

    override fun reloadTextSizeSelf() {
        // intentionally blank
    }

    override fun onBackPressed(): Boolean = content?.core?.onBackPressed() ?: false

    override fun onTabClick(): Unit = content?.core?.onTabClicked() ?: Unit
}

abstract class RecyclerFragment<T> : BaseFragment(), NativeFragmentContract<T> {


    override fun revertToWeb() {
        //todo
    }

}

open class WebFragment : BaseFragment(), FragmentContract {

    override val layoutRes: Int = R.layout.view_content_web

    /**
     * Given a webview, output a client
     */
    open fun client(web: FrostWebView) = FrostWebViewClient(web)

    override fun innerView(context: Context) = FrostWebView(context)

}

class WebFragmentMenu : WebFragment() {

    override fun client(web: FrostWebView) = FrostWebViewClientMenu(web)

}