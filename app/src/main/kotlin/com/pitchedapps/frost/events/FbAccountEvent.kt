package com.pitchedapps.frost.events

import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.web.FrostWebViewCore

/**
 * Created by Allan Wang on 2017-06-02.
 *
 * An emitter for whenever a change occurs relating to the active facebook account
 * All subscribers will call one of the execute methods below so the logic is handled within this class
 * [data]   [CookieModel] content
 * [sender] Webview position that sent the event; or -1 otherwise
 * [flag]   See companion object
 */
class FbAccountEvent(val data: CookieModel, val sender: Int, val flag: Int) {

    init {
        L.d(toString())
    }

    companion object {
        const val FLAG_LOGOUT = -2
        const val FLAG_RESET = -1
        const val FLAG_NEW = 0
        const val FLAG_SWITCH = 1
        const val FLAG_USER_NAME = 2
    }

    fun execute(webView: FrostWebViewCore) {
//        if (sender != -1 && sender == webView.position) return
//        when (flag) {
//            FLAG_LOGOUT, FLAG_RESET, FLAG_NEW, FLAG_SWITCH -> webView.loadBaseUrl()
//        }
    }

    /**
     * If new user id is found; create an account header and fetch the username
     * If the username is found and the current account is nameless, set the name
     * Ignore other flags
     */
    fun execute(accountHeader: AccountHeader) {
        when (flag) {
            FLAG_NEW -> {
                val profile = ProfileDrawerItem()
                        .withName(data.name)
                        .withIcon(PROFILE_PICTURE_URL(data.id))
                accountHeader.addProfile(profile, 0)
                accountHeader.setActiveProfile(profile, true)
//                if (data.name == null)
//                    UsernameFetcher.fetch(data, sender)
            }
            FLAG_USER_NAME -> {
                if (accountHeader.activeProfile.name == null)
                    accountHeader.activeProfile.withName(data.name)
            }
            FLAG_LOGOUT -> {

            }
        }
    }
}