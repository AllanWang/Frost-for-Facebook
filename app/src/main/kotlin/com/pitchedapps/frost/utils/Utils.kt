package com.pitchedapps.frost.utils

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.utils.*
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.pitchedapps.frost.*
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.services.NotificationService

/**
 * Created by Allan Wang on 2017-06-03.
 */
internal const val EXTRA_COOKIES = "extra_cookies"
internal const val ARG_URL = "arg_url"

fun Context.launchNewTask(clazz: Class<out Activity>, cookieList: ArrayList<CookieModel> = arrayListOf(), clearStack: Boolean = false) {
    startActivity(clazz, clearStack, {
        putParcelableArrayListExtra(EXTRA_COOKIES, cookieList)
    })
}

fun Context.launchLogin(cookieList: ArrayList<CookieModel>, clearStack: Boolean = true) {
    if (cookieList.isNotEmpty()) launchNewTask(SelectorActivity::class.java, cookieList, clearStack)
    else launchNewTask(LoginActivity::class.java, clearStack = clearStack)
}

fun Activity.cookies(): ArrayList<CookieModel> {
    return intent?.extras?.getParcelableArrayList<CookieModel>(EXTRA_COOKIES) ?: arrayListOf()
}

val String.formattedFbUrl: String
    get() {
        var url = this
        if (url.startsWith("#!/")) url = url.substring(2)
        if (url.startsWith('/')) url = FB_URL_BASE + url.substring(1)
        url = url.replace("/#!/", "/")
        val ref = url.indexOf("?ref")
        if (ref != -1) url = url.substring(0, ref)
        return url
    }

fun Context.launchWebOverlay(url: String) {
    val argUrl = url.formattedFbUrl
    L.i("Launch web overlay: $argUrl")
    startActivity(WebOverlayActivity::class.java, false, {
        putExtra(ARG_URL, argUrl)
    })
}

fun WebOverlayActivity.url(): String {
    return intent.extras?.getString(ARG_URL) ?: FbTab.FEED.url
}

val Context.frostNotification: NotificationCompat.Builder
    get() = NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).apply {
        setSmallIcon(R.drawable.frost_f_24)
        setAutoCancel(true)
        color = color(R.color.frost_notification_accent)
    }


fun Activity.materialDialogThemed(action: MaterialDialog.Builder.() -> Unit): MaterialDialog {
    val builder = MaterialDialog.Builder(this).theme()
    builder.action()
    return builder.show()
}

fun MaterialDialog.Builder.theme(): MaterialDialog.Builder {
    val dimmerTextColor = Prefs.textColor.adjustAlpha(0.8f)
    titleColor(Prefs.textColor)
    contentColor(dimmerTextColor)
    widgetColor(dimmerTextColor)
    backgroundColor(Prefs.bgColor.lighten(0.1f).withMinAlpha(200))
    positiveColor(Prefs.textColor)
    negativeColor(Prefs.textColor)
    neutralColor(Prefs.textColor)
    return this
}

fun Activity.setFrostTheme(forceTransparent: Boolean = false) {
    val isTransparent = (Color.alpha(Prefs.bgColor) != 255) || forceTransparent
    if (Prefs.bgColor.isColorDark())
        setTheme(if (isTransparent) R.style.FrostTheme_Transparent else R.style.FrostTheme)
    else
        setTheme(if (isTransparent) R.style.FrostTheme_Light_Transparent else R.style.FrostTheme_Light)
}

fun Activity.setFrostColors(toolbar: Toolbar? = null, themeWindow: Boolean = true,
                            texts: Array<TextView> = arrayOf(), headers: Array<View> = arrayOf(), backgrounds: Array<View> = arrayOf()) {
    val darkAccent = Prefs.headerColor.darken()
    statusBarColor = darkAccent.darken().withAlpha(255)
    navigationBarColor = darkAccent
    if (themeWindow) window.setBackgroundDrawable(ColorDrawable(Prefs.bgColor))
    toolbar?.setBackgroundColor(darkAccent)
    toolbar?.setTitleTextColor(Prefs.iconColor)
    toolbar?.overflowIcon?.setTint(Prefs.iconColor)
    texts.forEach { it.setTextColor(Prefs.textColor) }
    headers.forEach { it.setBackgroundColor(darkAccent) }
    backgrounds.forEach { it.setBackgroundColor(Prefs.bgColor) }
}


const val NOTIFICATION_JOB = 7
/**
 * [interval] is # of min, which must be at least 15
 * returns false if an error occurs; true otherwise
 */
fun Context.scheduleNotifications(minutes: Long): Boolean {
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.cancel(NOTIFICATION_JOB)
    if (minutes < 0L) return true
    val serviceComponent = ComponentName(this, NotificationService::class.java)
    val builder = JobInfo.Builder(NOTIFICATION_JOB, serviceComponent)
            .setPeriodic(minutes * 60000)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) //TODO add options
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.e("Notification scheduler failed")
        return false
    }
    return true
}

fun frostAnswers(action: Answers.() -> Unit) {
    if (BuildConfig.DEBUG) return
    //TODO add opt out toggle
    Answers.getInstance().action()
}

fun frostAnswersCustom(name: String, action: CustomEvent.() -> Unit = {}) {
    frostAnswers {
        logCustom(CustomEvent("Frost $name").apply { action() })
    }
}