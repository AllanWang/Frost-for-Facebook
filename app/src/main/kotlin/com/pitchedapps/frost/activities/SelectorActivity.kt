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

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.utils.bindView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.views.AccountItem
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 2017-06-04.
 */
class SelectorActivity : BaseActivity() {

    val recycler: RecyclerView by bindView(R.id.selector_recycler)
    val adapter = FastItemAdapter<AccountItem>()
    val text: AppCompatTextView by bindView(R.id.text_select_account)
    val container: ConstraintLayout by bindView(R.id.container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector)
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter
        adapter.add(cookies().map { AccountItem(it) })
        adapter.add(AccountItem(null)) // add account
        adapter.withEventHook(object : ClickEventHook<AccountItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? =
                (viewHolder as? AccountItem.ViewHolder)?.v

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<AccountItem>,
                item: AccountItem
            ) {
                if (item.cookie == null) this@SelectorActivity.launchNewTask<LoginActivity>()
                else launch {
                    FbCookie.switchUser(item.cookie)
                    launchNewTask<MainActivity>(cookies())
                }
            }
        })
        setFrostColors {
            text(text)
            background(container)
        }
    }
}
