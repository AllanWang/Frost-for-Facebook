package com.pitchedapps.frost.views

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.widget.FrameLayout
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-11-07.
 */
interface FrostRefreshContract: SwipeRefreshLayout.OnRefreshListener {
    /**
     * Call to reload wrapped data
     */
    fun refresh(animate: Boolean)

    /**
     * Observable to get data on whether view is refreshing or not
     */
    val refreshObservable: PublishSubject<Boolean>

    /**
     * Observable to get data on refresh progress, with range [0, 100]
     */
    val progressObservable: PublishSubject<Int>
}

class FrostRefreshView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), FrostRefreshContract {

}