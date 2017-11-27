package com.pitchedapps.frost.activities

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import ca.allanwang.kau.adapters.fastAdapter
import ca.allanwang.kau.animators.FadeScaleAnimatorAdd
import ca.allanwang.kau.animators.FadeScaleAnimatorRemove
import ca.allanwang.kau.animators.KauAnimator
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.kotlin.lazyContext
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.loadFbTabs
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.iitems.TabIItem
import com.pitchedapps.frost.utils.Prefs
import java.util.*

/**
 * Created by user7681 on 26/11/17.
 */
class TabCustomizerActivity : BaseActivity(), ItemTouchCallback {

    companion object {
        private const val maxPerRow = 4
        private const val scaleFactor = 0.7f
        private const val animDuration = 300L
        private val wobble = lazyContext { AnimationUtils.loadAnimation(it, R.anim.rotate_delta) }
    }

    val previewRv: RecyclerView by bindView(R.id.tab_preview)
    val instructions: TextView by bindView(R.id.instructions)
    val divider: View by bindView(R.id.divider)
    val optionsRv: RecyclerView by bindView(R.id.tab_options)

    val previewAdapter = FastItemAdapter<TabIItem>()
    val optionsAdapter = FastItemAdapter<TabIItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_customizer)

        previewRv.layoutManager = object : GridLayoutManager(this, maxPerRow, GridLayoutManager.VERTICAL, false) {
            override fun canScrollVertically() = false
            override fun canScrollHorizontally() = false
        }
        previewRv.adapter = previewAdapter
        previewRv.setBackgroundColor(Prefs.headerColor)
        previewRv.itemAnimator = animator
        ItemTouchHelper(SimpleDragCallback(SimpleDragCallback.LEFT_RIGHT, this)).attachToRecyclerView(previewRv)

        optionsRv.layoutManager = GridLayoutManager(this, maxPerRow, GridLayoutManager.VERTICAL, false)
        optionsRv.adapter = optionsAdapter
        optionsRv.itemAnimator = animator

        divider.setBackgroundColor(Prefs.textColor.withAlpha(30))
        instructions.setTextColor(Prefs.textColor)

        val current = loadFbTabs()
        previewAdapter.add(current.map { TabIItem(it, true) })

        val remaining = FbItem.values().toMutableList()
        remaining.removeAll(current)

        optionsAdapter.add(remaining.map { TabIItem(it, false) })

        previewAdapter.withOnClickListener { view, _, item, position -> onPreviewClick(view, item, position);true }
        optionsAdapter.withOnClickListener { view, _, item, position -> onOptionClick(view, item, position); true }
        optionsAdapter.withOnLongClickListener { view, _, item, position -> onOptionClick(view, item, position); true }
    }

    private val animator
        get() = KauAnimator(FadeScaleAnimatorAdd(scaleFactor, 0f),
                FadeScaleAnimatorRemove(scaleFactor, 0f)).apply {
            addDuration = animDuration
            changeDuration = animDuration
            removeDuration = animDuration
        }

    private fun View.wobble() = startAnimation(wobble(context))

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        Collections.swap(previewAdapter.adapterItems, oldPosition, newPosition)
        previewAdapter.notifyItemMoved(oldPosition, newPosition)
        return true
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {

    }

    private fun onOptionClick(view: View, item: TabIItem, position: Int) {
        if (previewAdapter.itemCount < maxPerRow) {
            optionsAdapter.remove(position)
            previewAdapter.add(item.asPreview())
        }
    }

    private var previewClick: Int = -1
    private var previewClickTime: Long = -1L

    private fun onPreviewClick(view: View, item: TabIItem, position: Int) {
        if (previewClick != position) {
            previewClick = position
        } else if (System.currentTimeMillis() - previewClickTime < 200) {
            if (previewAdapter.itemCount > 1) {
                previewAdapter.remove(position)
                optionsAdapter.add(item.asOption())
                previewClick = -1
            } else {
                view.wobble()
            }
        }
        previewClickTime = System.currentTimeMillis()
    }
}