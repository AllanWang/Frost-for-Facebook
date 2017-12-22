package com.pitchedapps.frost.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.fragments.RecyclerContentContract
import com.pitchedapps.frost.utils.L
import java.lang.ref.WeakReference

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr),
        FrostContentCore {

    override fun reload(animate: Boolean) = reloadBase(animate)

    override lateinit var parent: FrostContentParent

    override val currentUrl: String
        get() = parent.baseUrl

    lateinit var recyclerContract: WeakReference<RecyclerContentContract>

    override fun bind(container: FrostContentContainer): View {
        if (container !is RecyclerContentContract)
            throw IllegalStateException("FrostRecyclerView must bind to a container that is a RecyclerContentContract")
        this.recyclerContract = WeakReference(container)
        container.bind(this)
        return this
    }

    init {
        isNestedScrollingEnabled = true
    }

    override fun reloadBase(animate: Boolean) {
        val contract = recyclerContract.get()
        if (contract == null) {
            L.eThrow("Attempted to reload with invalid contract")
            return
        }
        contract.reload({ parent.progressObservable.onNext(it) }) {
            parent.progressObservable.onNext(100)
            parent.refreshObservable.onNext(false)
        }
    }

    override fun clearHistory() {
        // intentionally blank
    }

    override fun destroy() {
        // todo see if any
    }

    override fun onBackPressed() = false

    /**
     * If webview is already at the top, refresh
     * Otherwise scroll to top
     */
    override fun onTabClicked() {
        if (scrollY < 5) reloadBase(true)
        else scrollToTop()
    }

    private fun scrollToTop() {
        stopScroll()
        smoothScrollToPosition(0)
    }

    override var active: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            // todo
        }

    override fun reloadTheme() {
        reloadThemeSelf()
    }

    override fun reloadThemeSelf() {
        reload(false) // todo see if there's a better solution
    }

    override fun reloadTextSize() {
        reloadTextSizeSelf()
    }

    override fun reloadTextSizeSelf() {
        // todo
    }

}