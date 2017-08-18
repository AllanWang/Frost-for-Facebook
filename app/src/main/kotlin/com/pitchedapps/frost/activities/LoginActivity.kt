package com.pitchedapps.frost.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crashlytics.android.answers.LoginEvent
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.fetchUsername
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.web.LoginWebView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleToObservable
import io.reactivex.subjects.SingleSubject


/**
 * Created by Allan Wang on 2017-06-01.
 */
class LoginActivity : BaseActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val web: LoginWebView by bindView(R.id.login_webview)
    val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val textview: AppCompatTextView by bindView(R.id.textview)
    val profile: ImageView by bindView(R.id.profile)

    val profileObservable = SingleSubject.create<Boolean>()
    val usernameObservable = SingleSubject.create<String>()
    lateinit var profileLoader: RequestManager

    // Helper to set and enable swipeRefresh
    var refresh: Boolean
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
        setFrostColors(toolbar)
        web.loadLogin({ refresh = it != 100 }) {
            cookie ->
            L.d("Login found")
            web.fadeOut(onFinish = {
                profile.fadeIn()
                loadInfo(cookie)
            })
        }
        profileLoader = Glide.with(profile)
    }

    fun loadInfo(cookie: CookieModel) {
        refresh = true
        Observable.zip(SingleToObservable(profileObservable), SingleToObservable(usernameObservable),
                BiFunction<Boolean, String, Pair<Boolean, String>> { foundImage, name -> Pair(foundImage, name) })
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
            (foundImage, name) ->
            refresh = false
            if (!foundImage) {
                L.e("Could not get profile photo; Invalid userId?")
                L.i(null, cookie.toString())
            }
            textview.text = String.format(getString(R.string.welcome), name)
            textview.fadeIn()
            frostAnswers { logLogin(LoginEvent().putMethod("frost_browser").putSuccess(true)) }
            /*
             * The user may have logged into an account that is already in the database
             * We will let the db handle duplicates and load it now after the new account has been saved
             */
            loadFbCookiesAsync {
                cookies ->
                Handler().postDelayed({
                    launchNewTask(if (Showcase.intro) IntroActivity::class.java else MainActivity::class.java,
                            ArrayList(cookies), clearStack = true)
                }, 1000)
            }
        }
        loadProfile(cookie.id)
        loadUsername(cookie)
    }


    fun loadProfile(id: Long) {
        profileLoader.load(PROFILE_PICTURE_URL(id)).withRoundIcon().listener(object : RequestListener<Drawable> {
            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                profileObservable.onSuccess(true)
                return false
            }

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                e.logFrostAnswers( "Profile loading exception")
                profileObservable.onSuccess(false)
                return false
            }
        }).into(profile)
    }

    fun loadUsername(cookie: CookieModel) {
        cookie.fetchUsername { usernameObservable.onSuccess(it) }
    }
}