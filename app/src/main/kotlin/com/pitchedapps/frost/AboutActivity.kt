package com.pitchedapps.frost

import ca.allanwang.kau.about.AboutActivityBase
import ca.allanwang.kau.adapters.FastItemThemedAdapter
import ca.allanwang.kau.iitems.CardIItem
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.isColorVisibleOn
import ca.allanwang.kau.utils.withMinAlpha
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
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

    override fun getLibraries(libs: Libs): List<Library> {
        val include = arrayOf(
                "materialdialogs",
                "kotterknife",
                "glide",
                "jsoup"
        )
        /*
         * These are great libraries, but either aren't used directly or are too common to be listed
         * Give more emphasis on the unique libs!
         */
        val exclude = arrayOf(
                "GoogleMaterialDesignIcons",
                "intellijannotations",
                "MaterialDesignIconicIcons",
                "MaterialDesignIcons",
                "materialize",
                "appcompat_v7",
                "design",
                "recyclerview_v7",
                "support_v4"
        )
        val l = libs.prepareLibraries(this, include, exclude, true, true)
//        l.forEach { KL.d("Lib ${it.definedName}") }
        return l
    }

    override fun postInflateMainPage(adapter: FastItemThemedAdapter<IItem<*, *>>) {
        adapter.add(CardIItem {
            descRes = R.string.frost_description
        })
    }
}