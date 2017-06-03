package com.pitchedapps.frost

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
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
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.web.LoginWebView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject


/**
 * Created by Allan Wang on 2017-06-01.
 */
class LoginActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val web: LoginWebView by bindView(R.id.login_webview)
    val refresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val textview: AppCompatTextView by bindView(R.id.textview)
    val profile: ImageView by bindView(R.id.profile)

    val loginObservable = SingleSubject.create<CookieModel>()
    val progressObservable = BehaviorSubject.create<Int>()

    companion object {
        fun newInstance(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            val bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_right).toBundle()
            ContextCompat.startActivity(context, intent, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        web.loginObservable = loginObservable
        web.progressObservable = progressObservable
        loginObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            cookieModel ->
            Glide.with(this@LoginActivity).load(PROFILE_PICTURE_URL(cookieModel.id)).listener(object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

            }).into(profile)
        }
        progressObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val loading = it != 100
            if (loading) refresh.isEnabled = true
            refresh.isRefreshing = loading
            if (!loading) refresh.isEnabled = false
        }
        web.loadLogin()
    }


}