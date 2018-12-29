package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import org.junit.Test
import kotlin.test.assertTrue

class FbCookieTest {

    @Test
    fun managerAcceptsCookie() {
        assertTrue(CookieManager.getInstance().acceptCookie(), "Cookie manager should accept cookie by default")
    }
}