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
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.fetchUsername
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.glide.transform
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Showcase
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.logFrostEvent
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.web.LoginWebView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.SingleSubject

/**
 * Created by Allan Wang on 2017-06-01.
 */
class LoginActivity : BaseActivity() {

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val web: LoginWebView by bindView(R.id.login_webview)
    private val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    private val textview: AppCompatTextView by bindView(R.id.textview)
    private val profile: ImageView by bindView(R.id.profile)

    private val profileSubject = SingleSubject.create<Boolean>()
    private val usernameSubject = SingleSubject.create<String>()
    private lateinit var profileLoader: RequestManager

    // Helper to set and enable swipeRefresh
    private var refresh: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            if (value) swipeRefresh.isEnabled = true
            swipeRefresh.isRefreshing = value
            if (!value) swipeRefresh.isEnabled = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        setTitle(R.string.kau_login)
        setFrostColors {
            toolbar(toolbar)
        }
        web.loadLogin({ refresh = it != 100 }) { cookie ->
            L.d { "Login found" }
            FbCookie.save(cookie.id)
            web.fadeOut(onFinish = {
                profile.fadeIn()
                loadInfo(cookie)
            })
        }
        profileLoader = GlideApp.with(profile)
    }

    private fun loadInfo(cookie: CookieModel) {
        refresh = true
        Single.zip<Boolean, String, Pair<Boolean, String>>(
            profileSubject,
            usernameSubject,
            BiFunction(::Pair)
        )
            .observeOn(AndroidSchedulers.mainThread()).subscribe { (foundImage, name) ->
                refresh = false
                if (!foundImage) {
                    L.e { "Could not get profile photo; Invalid userId?" }
                    L._i { cookie }
                }
                textview.text = String.format(getString(R.string.welcome), name)
                textview.fadeIn()
                frostEvent("Login", "success" to true)
                /*
                 * The user may have logged into an account that is already in the database
                 * We will let the db handle duplicates and load it now after the new account has been saved
                 */
                loadFbCookiesAsync {
                    val cookies = ArrayList(it)
                    Handler().postDelayed({
                        if (Showcase.intro)
                            launchNewTask<IntroActivity>(cookies, true)
                        else
                            launchNewTask<MainActivity>(cookies, true)
                    }, 1000)
                }
            }.disposeOnDestroy()
        loadProfile(cookie.id)
        loadUsername(cookie)
    }

    private fun loadProfile(id: Long) {
        profileLoader.load(profilePictureUrl(id))
            .transform(FrostGlide.roundCorner).listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    profileSubject.onSuccess(true)
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e.logFrostEvent("Profile loading exception")
                    profileSubject.onSuccess(false)
                    return false
                }
            }).into(profile)
    }

    private fun loadUsername(cookie: CookieModel) {
        cookie.fetchUsername(usernameSubject::onSuccess).disposeOnDestroy()
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
