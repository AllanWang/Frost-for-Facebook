package com.pitchedapps.frost.utils

import android.content.Intent
import android.os.BaseBundle

/**
 * Created by Allan Wang on 29/12/17.
 */
interface EnumBundle<E : Enum<E>> {

    val bundleContract: EnumBundleCompanion<E>

    val ordinal: Int

    fun put(intent: Intent) {
        intent.putExtra(bundleContract.argTag, ordinal)
    }

    fun put(bundle: BaseBundle?) {
        bundle?.putInt(bundleContract.argTag, ordinal)
    }
}

interface EnumBundleCompanion<E : Enum<E>> {

    val argTag: String

    val values: Array<E>

    operator fun get(index: Int) = values.getOrNull(index)

    operator fun get(bundle: BaseBundle?) = get(bundle?.getInt(argTag, -1) ?: -1)

    operator fun get(intent: Intent?) = get(intent?.getIntExtra(argTag, -1) ?: -1)

}