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
package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.util.TypedValue
import ca.allanwang.kau.kpref.activity.items.KPrefSeekbar
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-07-07.
 */
class KPrefTextSeekbar(builder: KPrefSeekbarContract) : KPrefSeekbar(builder) {

    var descOriginalSize = 1f

    init {
        with(builder) {
            min = 50
            max = 200
            descRes = R.string.web_text_scaling_desc
            textViewConfigs = {
                minEms = 2
                setOnLongClickListener {
                    pref = 100
                    reloadSelf()
                    true
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        descOriginalSize = holder.desc?.textSize ?: 1f
        holder.desc?.layoutParams
        builder.toText = {
            holder.desc?.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                descOriginalSize * it.toFloat() / 100
            )
            "$it%"
        }
    }

    override fun unbindView(holder: ViewHolder) {
        holder.desc?.setTextSize(TypedValue.COMPLEX_UNIT_PX, descOriginalSize)
        super.unbindView(holder)
    }
}
