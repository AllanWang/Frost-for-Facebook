package com.pitchedapps.frost.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.fragments.RecyclerContentContract

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

    lateinit var recyclerContract: RecyclerContentContract

    init {
        layoutManager = LinearLayoutManager(context)
    }

    override fun bind(container: FrostContentContainer): View {
        if (container !is RecyclerContentContract)
            throw IllegalStateException("FrostRecyclerView must bind to a container that is a RecyclerContentContract")
        this.recyclerContract = container
        container.bind(this)
        return this
    }

    init {
        isNestedScrollingEnabled = true
    }

    override fun reloadBase(animate: Boolean) {
        parent.refreshObservable.onNext(true)
        recyclerContract.reload({ parent.progressObservable.onNext(it) }) {
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
     * If recycler is already at the top, refresh
     * Otherwise scroll to top
     */
    override fun onTabClicked() {
        val firstPosition = (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (firstPosition == 0) reloadBase(true)
        else scrollToTop()
    }

    private fun scrollToTop() {
        stopScroll()
        smoothScrollToPosition(0)
    }

    // nothing running in background; no need to listen
    override var active: Boolean = true

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