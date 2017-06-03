package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.events.FbAccountEvent
import com.pitchedapps.frost.utils.L
import org.greenrobot.eventbus.EventBus
import org.jsoup.Jsoup
import kotlin.concurrent.thread

/**
 * Created by Allan Wang on 2017-06-02.
 */
object UsernameFetcher {

    fun fetch(data: CookieModel, sender: Int) {
        thread {
            try {
                val title = Jsoup.connect(FbTab.PROFILE.url)
                        .cookie(FACEBOOK_COM, data.cookie)
                        .get().title()
                L.d("User name found: $title")
                data.name = title
            } catch (e: Exception) {
                L.e("User name fetching failed: ${e.message}")
                data.name = ""
            } finally {
                if (data.name != null) {
                    saveFbCookie(data)
                    EventBus.getDefault().post(FbAccountEvent(data, sender, FbAccountEvent.FLAG_USER_NAME))
                }
            }
        }
    }

}