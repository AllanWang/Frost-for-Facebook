package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.ButterKnife
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.views.AccountItem

/**
 * Created by Allan Wang on 2017-06-04.
 */
class SelectorActivity : AppCompatActivity() {

    val recycler: RecyclerView by bindView(R.id.selector_recycler)
    val adapter = FastItemAdapter<AccountItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector)
        ButterKnife.bind(this)
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter
        adapter.add(cookies().map { AccountItem(it) })
        adapter.add(AccountItem(null)) // add account
        adapter.withItemEvent(object : ClickEventHook<AccountItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View?
                    = if (viewHolder is AccountItem.ViewHolder) viewHolder.v else null

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<AccountItem>, item: AccountItem) {
                if (item.cookie == null) this@SelectorActivity.launchNewTask(LoginActivity::class.java)
                else FbCookie.switchUser(item.cookie, { launchNewTask(MainActivity::class.java, cookies()) })
            }
        })
    }
}