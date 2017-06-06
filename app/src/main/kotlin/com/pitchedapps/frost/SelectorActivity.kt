package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import butterknife.ButterKnife
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.cookies
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
        recycler.adapter = adapter
        adapter.add(cookies().map { AccountItem(it) })
        adapter.add(AccountItem()) // add account
    }
}