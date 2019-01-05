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

import android.content.res.Configuration
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.searchview.SearchViewHolder
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.utils.setFrostTheme

/**
 * Created by Allan Wang on 2017-06-12.
 */
abstract class BaseActivity : KauBaseActivity() {

    /**
     * Inherited consumer to customize back press
     */
    protected open fun backConsumer(): Boolean = false

    final override fun onBackPressed() {
        if (this is SearchViewHolder && searchViewOnBackPress()) return
        if (this is VideoViewHolder && videoOnBackPress()) return
        if (backConsumer()) return
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this !is WebOverlayActivityBase) setFrostTheme()
    }

    override fun onStop() {
        if (this is VideoViewHolder) videoOnStop()
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (this is VideoViewHolder) videoViewer?.updateLocation()
    }
}
