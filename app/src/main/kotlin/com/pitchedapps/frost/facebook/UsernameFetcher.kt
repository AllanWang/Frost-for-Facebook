package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.SingleSubject
import org.jsoup.Jsoup
import kotlin.concurrent.thread

/**
 * Created by Allan Wang on 2017-06-02.
 */
object UsernameFetcher {

    fun fetch(data: CookieModel, callback: SingleSubject<String>) {
        thread {
            var name = ""
            try {
                name = Jsoup.connect(FbTab.PROFILE.url)
                        .cookie(FACEBOOK_COM, data.cookie)
                        .get().title()
                L.d("User name found: $name")
            } catch (e: Exception) {
                L.e(e, "User name fetching failed")
            } finally {
                data.name = name
                saveFbCookie(data)
                callback.onSuccess(name)
            }
        }
    }

}