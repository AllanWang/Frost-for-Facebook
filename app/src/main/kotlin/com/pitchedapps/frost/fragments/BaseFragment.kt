package com.pitchedapps.frost.fragments

import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import com.pitchedapps.frost.utils.putInt
import com.pitchedapps.frost.utils.refWatch

/**
 * Created by Allan Wang on 2017-05-29.
 */
interface BaseFragmentContract {
    fun onBackPressed(): Boolean
}

abstract class BaseFragment : Fragment(), BaseFragmentContract {
    val position: Int by lazy { arguments.getInt(ARG_POSITION) }

    companion object {
        val ARG_POSITION = "arg_position"

        fun <T : BaseFragment> newInstance(fragment: T, position: Int): T {
            fragment.putInt(ARG_POSITION, position)
            return fragment
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        refWatch()
//    }
}