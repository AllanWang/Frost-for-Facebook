package com.pitchedapps.frost.settings

import android.os.Bundle
import android.support.v7.preference.AndroidResources
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pitchedapps.frost.views.RippleCanvas
import com.pitchedapps.frost.views.matchParent

/**
 * Created by Allan Wang on 2017-05-31.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var ripple: RippleCanvas

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val frame = view.findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER) as ViewGroup
        ripple = RippleCanvas(frame.context)
        ripple.matchParent()
        frame.addView(ripple, 0)
        return view
    }
}