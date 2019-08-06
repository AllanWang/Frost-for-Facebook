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
package com.pitchedapps.frost.fragments

import ca.allanwang.kau.adapters.fastAdapter
import ca.allanwang.kau.utils.withMainContext
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.parsers.FrostParser
import com.pitchedapps.frost.facebook.parsers.ParseData
import com.pitchedapps.frost.facebook.parsers.ParseResponse
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.views.FrostRecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Allan Wang on 27/12/17.
 */
abstract class RecyclerFragment<T, Item : IItem<*, *>> : BaseFragment(), RecyclerContentContract {

    override val layoutRes: Int = R.layout.view_content_recycler

    abstract val adapter: ModelAdapter<T, Item>

    override fun firstLoadRequest() {
        val core = core ?: return
        if (firstLoad) {
            core.reloadBase(true)
            firstLoad = false
        }
    }

    final override suspend fun reload(progress: (Int) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            val data = try {
                reloadImpl(progress)
            } catch (e: Exception) {
                L.e(e) { "Recycler reload fail $baseUrl" }
                null
            }
            withMainContext {
                if (data == null) {
                    valid = false
                    false
                } else {
                    adapter.setNewList(data)
                    true
                }
            }
        }

    protected abstract suspend fun reloadImpl(progress: (Int) -> Unit): List<T>?
}

abstract class GenericRecyclerFragment<T, Item : IItem<*, *>> : RecyclerFragment<T, Item>() {

    abstract fun mapper(data: T): Item

    override val adapter: ModelAdapter<T, Item> = ModelAdapter { this.mapper(it) }

    final override fun bind(recyclerView: FrostRecyclerView) {
        recyclerView.adapter = getAdapter()
        recyclerView.onReloadClear = { adapter.clear() }
        bindImpl(recyclerView)
    }

    /**
     * Anything to call for one time bindings
     * At this stage, all adapters will have FastAdapter references
     */
    open fun bindImpl(recyclerView: FrostRecyclerView) = Unit

    /**
     * Create the fast adapter to bind to the recyclerview
     */
    open fun getAdapter(): FastAdapter<IItem<*, *>> = fastAdapter(this.adapter)
}

abstract class FrostParserFragment<T : ParseData, Item : IItem<*, *>> :
    RecyclerFragment<Item, Item>() {

    /**
     * The parser to make this all happen
     */
    abstract val parser: FrostParser<T>

    open fun getDoc(cookie: String?) = frostJsoup(cookie, parser.url)

    abstract fun toItems(response: ParseResponse<T>): List<Item>

    override val adapter: ItemAdapter<Item> = ItemAdapter()

    final override fun bind(recyclerView: FrostRecyclerView) {
        recyclerView.adapter = getAdapter()
        recyclerView.onReloadClear = { adapter.clear() }
        bindImpl(recyclerView)
    }

    /**
     * Anything to call for one time bindings
     * At this stage, all adapters will have FastAdapter references
     */
    open fun bindImpl(recyclerView: FrostRecyclerView) = Unit

    /**
     * Create the fast adapter to bind to the recyclerview
     */
    open fun getAdapter(): FastAdapter<IItem<*, *>> = fastAdapter(this.adapter)

    override suspend fun reloadImpl(progress: (Int) -> Unit): List<Item>? =
        withContext(Dispatchers.IO) {
            progress(10)
            val cookie = FbCookie.webCookie
            val doc = getDoc(cookie)
            progress(60)
            val response = try {
                parser.parse(cookie, doc)
            } catch (ignored: Exception) {
                null
            }
            if (response == null) {
                L.i { "RecyclerFragment failed for ${baseEnum.name}" }
                L._d { "Cookie used: $cookie" }
                return@withContext null
            }
            progress(80)
            val items = toItems(response)
            progress(97)
            return@withContext items
        }
}
