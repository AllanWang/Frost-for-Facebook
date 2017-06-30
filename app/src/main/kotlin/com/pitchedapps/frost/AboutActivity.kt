package com.pitchedapps.frost

import ca.allanwang.kau.about.AboutActivityBase
import ca.allanwang.kau.adapters.FastItemThemedAdapter
import ca.allanwang.kau.iitems.CardIItem
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.withMinAlpha
import com.mikepenz.fastadapter.IItem
import com.pitchedapps.frost.utils.Prefs


/**
 * Created by Allan Wang on 2017-06-26.
 */
class AboutActivity : AboutActivityBase(R.string::class.java, configBuilder = {
    textColor = Prefs.textColor
    accentColor = Prefs.accentColor
    backgroundColor = Prefs.bgColor.withMinAlpha(200)
    cutoutForeground = if (0xff3b5998.toInt().isColorVisibleOn(Prefs.bgColor)) 0xff3b5998.toInt() else Prefs.accentColor
    cutoutDrawableRes = R.drawable.frost_f_256
}) {
    override fun postInflateMainPage(adapter: FastItemThemedAdapter<IItem<*, *>>) {
        adapter.add(CardIItem {
            descRes = R.string.frost_description
        })
    }
}