package com.pitchedapps.frost.fragments

import android.content.Context
import android.support.v4.app.Fragment
import com.pitchedapps.frost.utils.KeyPairObservable
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.putInt
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

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

}