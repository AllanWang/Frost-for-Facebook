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
            holder.desc?.setTextSize(TypedValue.COMPLEX_UNIT_PX, descOriginalSize * it.toFloat() / 100)
            "$it%"
        }
    }

    override fun unbindView(holder: ViewHolder) {
        holder.desc?.setTextSize(TypedValue.COMPLEX_UNIT_PX, descOriginalSize)
        super.unbindView(holder)
    }
}