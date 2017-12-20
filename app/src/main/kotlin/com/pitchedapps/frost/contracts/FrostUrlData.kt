package com.pitchedapps.frost.contracts

import com.pitchedapps.frost.facebook.FbItem

/**
 * Created by Allan Wang on 19/12/17.
 */
interface FrostUrlData {

    /**
     * The main (and fallback) url
     */
    var baseUrl: String

    /**
     * Only base viewpager should pass an enum
     */
    var baseEnum: FbItem?

    fun passUrlDataTo(other : FrostUrlData) {
        other.baseUrl = baseUrl
        other.baseEnum = baseEnum
    }

}