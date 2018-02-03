package com.pitchedapps.frost.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.fadeScaleTransition
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.withArguments
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.contracts.DynamicUiContract
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.MainFabContract
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-11-07.
 *
 * All fragments pertaining to the main view
 * Must be attached to activities implementing [MainActivityContract]
 */
abstract class BaseFragment : Fragment(), FragmentContract, DynamicUiContract {

    companion object {
        private const val ARG_POSITION = "arg_position"
        private const val ARG_VALID = "arg_valid"

        internal operator fun invoke(base: () -> BaseFragment, useFallback: Boolean, data: FbItem, position: Int): BaseFragment {
            val fragment = if (!useFallback) base() else WebFragment()
            val d = if (data == FbItem.FEED) FeedSort(Prefs.feedSort).item else data
            fragment.withArguments(
                    ARG_URL to d.url,
                    ARG_POSITION to position
            )
            d.put(fragment.arguments!!)
            return fragment
        }
    }

    override val baseUrl: String by lazy { arguments!!.getString(ARG_URL) }
    override val baseEnum: FbItem by lazy { FbItem[arguments]!! }
    override val position: Int by lazy { arguments!!.getInt(ARG_POSITION) }

    override var valid: Boolean
        get() = arguments!!.getBoolean(ARG_VALID, true)
        set(value) {
            if (value || this is WebFragment) return
            arguments!!.putBoolean(ARG_VALID, value)
            L.e { "Invalidating position $position" }
            frostAnswersCustom("Native Fallback",
                    "Item" to baseEnum.name)
            (context as MainActivityContract).reloadFragment(this)
        }

    override var firstLoad: Boolean = true
    private var activityDisposable: Disposable? = null
    private var onCreateRunnable: ((FragmentContract) -> Unit)? = null

    override var content: FrostContentParent? = null

    protected abstract val layoutRes: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstLoad = true
        if (context !is MainActivityContract)
            throw IllegalArgumentException("${this::class.java.simpleName} is not attached to a context implementing MainActivityContract")
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        val core = core ?: return
        if (userVisibleHint && isVisible && firstLoad) {
            core.reloadBase(true)
            firstLoad = false
        }
    }

    override fun post(action: (fragment: FragmentContract) -> Unit) {
        onCreateRunnable = action
    }

    override fun setTitle(title: String) {
        (context as? MainActivityContract)?.setTitle(title)
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
                        contract.setTitle(baseEnum.titleId)
                        updateFab(contract)
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

    override fun updateFab(contract: MainFabContract) {
        contract.hideFab() // default
    }

    protected fun FloatingActionButton.update(iicon: IIcon, click: () -> Unit) {
        if (isShown) {
            fadeScaleTransition {
                setIcon(iicon, Prefs.iconColor)
            }
        } else {
            setIcon(iicon, Prefs.iconColor)
            show()
        }
        setOnClickListener { click() }
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
        L.i { "Fragment on destroy $position ${hashCode()}" }
        content?.destroy()
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

