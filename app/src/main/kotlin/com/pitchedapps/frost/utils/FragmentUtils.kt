package com.pitchedapps.frost.utils

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by Allan Wang on 2017-05-29.
 */

fun Fragment.withBundle(creator: (Bundle) -> Unit): Fragment {
    val bundle = Bundle()
    creator.invoke(bundle)
    this.arguments = bundle
    return this
}