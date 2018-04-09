package com.pitchedapps.frost.activities

import android.os.Bundle
import ca.allanwang.kau.ui.activities.SwipeRecyclerActivity
import ca.allanwang.kau.ui.views.SwipeRecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.pitchedapps.frost.iitems.ReleaseIItem
import com.pitchedapps.frost.services.UpdateManager
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.uiThread

/**
 * Created by Allan Wang on 07/04/18.
 */
class UpdateActivity : SwipeRecyclerActivity<ReleaseIItem>() {

    override fun onCreate(savedInstanceState: Bundle?, fastAdapter: FastAdapter<ReleaseIItem>) {
        fastAdapter.withOnClickListener { _, _, item, _ ->
            if (item is ReleaseIItem) {
                // todo download
            }
            true
        }
    }

    override fun AnkoAsyncContext<SwipeRecyclerView>.onRefresh() {
        val release = UpdateManager.getLatestGithubRelease() ?: return
        uiThread { adapter.set(listOf(ReleaseIItem(release))) }
    }

}