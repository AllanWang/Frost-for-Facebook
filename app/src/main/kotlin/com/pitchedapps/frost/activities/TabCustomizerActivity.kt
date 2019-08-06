/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.activities

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.kotlin.lazyContext
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.scaleXY
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.db.GenericDao
import com.pitchedapps.frost.db.TAB_COUNT
import com.pitchedapps.frost.db.getTabs
import com.pitchedapps.frost.db.saveTabs
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.iitems.TabIItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.setFrostColors
import kotlinx.android.synthetic.main.activity_tab_customizer.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Collections

/**
 * Created by Allan Wang on 26/11/17.
 */
class TabCustomizerActivity : BaseActivity() {

    private val genericDao: GenericDao by inject()

    private val adapter = FastItemAdapter<TabIItem>()

    private val wobble = lazyContext { AnimationUtils.loadAnimation(it, R.anim.rotate_delta) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_customizer)

        pseudo_toolbar.setBackgroundColor(Prefs.headerColor)

        tab_recycler.layoutManager =
            GridLayoutManager(this, TAB_COUNT, RecyclerView.VERTICAL, false)
        tab_recycler.adapter = adapter
        tab_recycler.setHasFixedSize(true)

        divider.setBackgroundColor(Prefs.textColor.withAlpha(30))
        instructions.setTextColor(Prefs.textColor)

        launch {
            val tabs = genericDao.getTabs().toMutableList()
            L.d { "Tabs $tabs" }
            val remaining = FbItem.values().filter { it.name[0] != '_' }.toMutableList()
            remaining.removeAll(tabs)
            tabs.addAll(remaining)
            adapter.set(tabs.map(::TabIItem))

            bindSwapper(adapter, tab_recycler)

            adapter.withOnClickListener { view, _, _, _ -> view!!.wobble(); true }
        }

        setResult(Activity.RESULT_CANCELED)

        fab_save.setIcon(GoogleMaterial.Icon.gmd_check, Prefs.iconColor)
        fab_save.backgroundTintList = ColorStateList.valueOf(Prefs.accentColor)
        fab_save.setOnClickListener {
            launchMain(NonCancellable) {
                val tabs = adapter.adapterItems.subList(0, TAB_COUNT).map(TabIItem::item)
                genericDao.saveTabs(tabs)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        fab_cancel.setIcon(GoogleMaterial.Icon.gmd_close, Prefs.iconColor)
        fab_cancel.backgroundTintList = ColorStateList.valueOf(Prefs.accentColor)
        fab_cancel.setOnClickListener { finish() }
        setFrostColors {
            themeWindow = true
        }
    }

    private fun View.wobble() = startAnimation(wobble(context))

    private fun bindSwapper(adapter: FastItemAdapter<*>, recycler: RecyclerView) {
        val dragCallback = TabDragCallback(SimpleDragCallback.ALL, swapper(adapter))
        ItemTouchHelper(dragCallback).attachToRecyclerView(recycler)
    }

    private fun swapper(adapter: FastItemAdapter<*>) = object : ItemTouchCallback {
        override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
            Collections.swap(adapter.adapterItems, oldPosition, newPosition)
            adapter.notifyAdapterDataSetChanged()
            return true
        }

        override fun itemTouchDropped(oldPosition: Int, newPosition: Int) = Unit
    }

    private class TabDragCallback(
        directions: Int,
        itemTouchCallback: ItemTouchCallback
    ) : SimpleDragCallback(directions, itemTouchCallback) {

        private var draggingView: TabIItem.ViewHolder? = null

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    (viewHolder as? TabIItem.ViewHolder)?.apply {
                        draggingView = this
                        itemView.animate().scaleXY(1.3f)
                        text.animate().alpha(0f)
                    }
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    draggingView?.apply {
                        itemView.animate().scaleXY(1f)
                        text.animate().alpha(1f)
                    }
                    draggingView = null
                }
            }
        }
    }
}
