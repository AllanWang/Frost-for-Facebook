package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.util.TypedValue
import ca.allanwang.kau.kpref.items.KPrefSeekbar
import ca.allanwang.kau.logging.KL
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
    override fun onPostBindView(viewHolder: ViewHolder, textColor: Int?, accentColor: Int?) {
        descOriginalSize = viewHolder.desc?.textSize ?: 1f
        viewHolder.desc?.layoutParams
        KL.d("Text size $descOriginalSize")
        KL.d("Text size ${viewHolder.title.textSize}")
        builder.toText = {
            KL.d("Ratio ${it.toFloat() / 100}")
            viewHolder.desc?.setTextSize(TypedValue.COMPLEX_UNIT_PX, descOriginalSize * it.toFloat() / 100)
            "$it%"
        }

        super.onPostBindView(viewHolder, textColor, accentColor)
    }

    override fun unbindView(holder: ViewHolder) {
        holder.desc?.setTextSize(TypedValue.COMPLEX_UNIT_PX, descOriginalSize)
        super.unbindView(holder)
    }
}