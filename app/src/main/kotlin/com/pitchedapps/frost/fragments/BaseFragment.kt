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
    fun onActivityEvent(position: Int, key: Int)
    fun onBackPressed(): Boolean
}

abstract class BaseFragment : Fragment(), Consumer<Pair<Int, Int>>, BaseFragmentContract {
    var disposable: Disposable? = null
    val position: Int by lazy { arguments.getInt(ARG_POSITION) }

    companion object {
        val ARG_POSITION = "arg_position"

        fun <T : BaseFragment> newInstance(fragment: T, position: Int): T {
            fragment.putInt(ARG_POSITION, position)
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity is KeyPairObservable && disposable == null)
            disposable = (activity as KeyPairObservable).observable.subscribe(this, Consumer {
                t: Throwable ->
                L.e(t.message ?: "Observable error")
            })
    }

    override fun onDestroyView() {
        disposable?.dispose()
        disposable = null
        super.onDestroyView()
    }

    override fun accept(t: Pair<Int, Int>) = onActivityEvent(t.first, t.second)

}