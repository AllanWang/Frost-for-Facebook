package com.pitchedapps.frost

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.views.fadeIn
import com.pitchedapps.frost.views.fadeOut
import com.pitchedapps.frost.views.setTextWithFade
import com.pitchedapps.frost.web.LoginWebView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleToObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject
import org.jsoup.Jsoup
import kotlin.concurrent.thread


/**
 * Created by Allan Wang on 2017-06-01.
 */
class LoginActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val web: LoginWebView by bindView(R.id.login_webview)
    val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val textview: AppCompatTextView by bindView(R.id.textview)
    val profile: ImageView by bindView(R.id.profile)

    val loginObservable = SingleSubject.create<CookieModel>()!!
    val progressObservable = BehaviorSubject.create<Int>()!!
    val profileObservable = SingleSubject.create<Boolean>()!!
    val usernameObservable = SingleSubject.create<String>()!!

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
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        setTitle(R.string.login)
        web.loginObservable = loginObservable
        web.progressObservable = progressObservable
        loginObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            cookie ->
            web.fadeOut(onFinish = {
                profile.fadeIn()
                textview.fadeIn()
                loadInfo(cookie)
            })
        }
        progressObservable.observeOn(AndroidSchedulers.mainThread()).subscribe { refresh = it != 100 }
        web.loadLogin()
    }

    fun loadInfo(cookie: CookieModel) {
        refresh = true
        Observable.zip(SingleToObservable(profileObservable), SingleToObservable(usernameObservable),
                BiFunction<Boolean, String, Pair<Boolean, String>> { foundImage, name -> Pair(foundImage, name) })
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
            (foundImage, name) ->
            refresh = false
            L.d("Zip done")
            if (!foundImage) L.e("Could not get profile photo; Invalid id?\n\t$cookie")
            textview.setTextWithFade(String.format(getString(R.string.welcome), name), duration = 500)
            /*
             * The user may have logged into an account that is already in the database
             * We will let the db handle duplicates and load it now after the new account has been saved
             */
            loadFbCookiesAsync {
                cookies ->
                Handler().postDelayed({
                    launchNewTask(MainActivity::class.java, ArrayList(cookies))
                }, 1000)
            }
        }
        loadProfile(cookie.id)
        loadUsername(cookie)
    }


    fun loadProfile(id: Long) {
        Glide.with(this@LoginActivity).load(PROFILE_PICTURE_URL(id)).listener(object : RequestListener<Drawable> {
            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                profileObservable.onSuccess(true)
                return false
            }

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                profileObservable.onSuccess(false)
                return false
            }
        }).into(profile)
    }

    fun loadUsername(cookie: CookieModel) {
        thread {
            var name = ""
            try {
                name = Jsoup.connect(FbTab.PROFILE.url)
                        .cookie(FACEBOOK_COM, cookie.cookie)
                        .get().title()
                L.d("User name found: $name")
            } catch (e: Exception) {
                L.e("User name fetching failed: ${e.message}")
            } finally {
                cookie.name = name
                saveFbCookie(cookie)
                usernameObservable.onSuccess(name)
            }
        }
    }
}