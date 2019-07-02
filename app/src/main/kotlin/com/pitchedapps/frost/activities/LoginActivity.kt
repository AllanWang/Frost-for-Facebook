/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.withMainContext
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.pitchedapps.frost.R
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.db.save
import com.pitchedapps.frost.db.selectAll
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Showcase
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.logFrostEvent
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.utils.uniqueOnly
import com.pitchedapps.frost.web.LoginWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.android.ext.android.inject
import java.net.UnknownHostException
import kotlin.coroutines.resume

/**
 * Created by Allan Wang on 2017-06-01.
 */
class LoginActivity : BaseActivity() {

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val web: LoginWebView by bindView(R.id.login_webview)
    private val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    private val textview: AppCompatTextView by bindView(R.id.textview)
    private val profile: ImageView by bindView(R.id.profile)
    private val cookieDao: CookieDao by inject()

    private lateinit var profileLoader: RequestManager
    private val refreshChannel = Channel<Boolean>(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        setTitle(R.string.kau_login)
        setFrostColors {
            toolbar(toolbar)
        }
        profileLoader = GlideApp.with(profile)
        launch {
            for (refreshing in refreshChannel.uniqueOnly(this)) {
                if (refreshing) swipeRefresh.isEnabled = true
                swipeRefresh.isRefreshing = refreshing
                if (!refreshing) swipeRefresh.isEnabled = false
            }
        }
        launch {
            val cookie = web.loadLogin { refresh(it != 100) }.await()
            L.d { "Login found" }
            FbCookie.save(cookie.id)
            webFadeOut()
            profile.fadeIn()
            loadInfo(cookie)
        }
    }

    private suspend fun webFadeOut(): Unit = suspendCancellableCoroutine { cont ->
        web.fadeOut { cont.resume(Unit) }
    }

    private fun refresh(refreshing: Boolean) {
        refreshChannel.offer(refreshing)
    }

    private suspend fun loadInfo(cookie: CookieEntity): Unit = withMainContext {
        refresh(true)

        val imageDeferred = async { loadProfile(cookie.id) }
        val nameDeferred = async { loadUsername(cookie) }

        val name: String? = nameDeferred.await()
        val foundImage: Boolean = imageDeferred.await()

        L._d { "Logged in and received data" }
        refresh(false)

        if (!foundImage) {
            L.e { "Could not get profile photo; Invalid userId?" }
            L._i { cookie }
        }

        textview.text = String.format(getString(R.string.welcome), name ?: "")
        textview.fadeIn()
        frostEvent("Login", "success" to true)

        /*
         * The user may have logged into an account that is already in the database
         * We will let the db handle duplicates and load it now after the new account has been saved
         */
        val cookies = ArrayList(cookieDao.selectAll())
        delay(1000)
        if (Showcase.intro)
            launchNewTask<IntroActivity>(cookies, true)
        else
            launchNewTask<MainActivity>(cookies, true)
    }

    private suspend fun loadProfile(id: Long): Boolean = withMainContext {
        suspendCancellableCoroutine<Boolean> { cont ->
            profileLoader.load(profilePictureUrl(id))
                .transform(FrostGlide.circleCrop).listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        cont.resume(true)
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        e.logFrostEvent("Profile loading exception")
                        cont.resume(false)
                        return false
                    }
                }).into(profile)
        }
    }

    private suspend fun loadUsername(cookie: CookieEntity): String? = withContext(Dispatchers.IO) {
        val result: String? = try {
            withTimeout(5000) {
                frostJsoup(cookie.cookie, FbItem.PROFILE.url).title()
            }
        } catch (e: Exception) {
            if (e !is UnknownHostException)
                e.logFrostEvent("Fetch username failed")
            null
        }

        if (result != null) {
            cookieDao.save(cookie.copy(name = result))
            return@withContext result
        }

        return@withContext cookie.name
    }

    override fun backConsumer(): Boolean {
        if (web.canGoBack()) {
            web.goBack()
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        web.resumeTimers()
    }

    override fun onPause() {
        web.pauseTimers()
        super.onPause()
    }
}
