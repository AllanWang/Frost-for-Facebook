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

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ca.allanwang.kau.searchview.SearchItem
import ca.allanwang.kau.searchview.SearchView
import ca.allanwang.kau.searchview.SearchViewHolder
import ca.allanwang.kau.searchview.bindSearchView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.fadeScaleTransition
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.setMenuIcons
import ca.allanwang.kau.utils.showIf
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toast
import ca.allanwang.kau.utils.withMinAlpha
import co.zsmb.materialdrawerkt.builders.Builder
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import co.zsmb.materialdrawerkt.draweritems.profile.profileSetting
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.FileChooserContract
import com.pitchedapps.frost.contracts.FileChooserDelegate
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.dbflow.TAB_COUNT
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.loadFbTabs
import com.pitchedapps.frost.enums.MainActivityLayout
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostSearch
import com.pitchedapps.frost.facebook.parsers.SearchParser
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.fragments.BaseFragment
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.ACTIVITY_SETTINGS
import com.pitchedapps.frost.utils.EXTRA_COOKIES
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.MAIN_TIMEOUT_DURATION
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_NAV
import com.pitchedapps.frost.utils.REQUEST_REFRESH
import com.pitchedapps.frost.utils.REQUEST_RESTART
import com.pitchedapps.frost.utils.REQUEST_RESTART_APPLICATION
import com.pitchedapps.frost.utils.REQUEST_SEARCH
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.frostChangelog
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.frostNavigationBar
import com.pitchedapps.frost.utils.launchLogin
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.launchWebOverlay
import com.pitchedapps.frost.utils.materialDialogThemed
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.views.BadgedIcon
import com.pitchedapps.frost.views.FrostVideoViewer
import com.pitchedapps.frost.views.FrostViewPager

/**
 * Created by Allan Wang on 20/12/17.
 *
 * Most of the logic that is unrelated to handling fragments
 */
abstract class BaseMainActivity : BaseActivity(), MainActivityContract,
    FileChooserContract by FileChooserDelegate(),
    VideoViewHolder, SearchViewHolder {

    protected lateinit var adapter: SectionsPagerAdapter
    override val frameWrapper: FrameLayout by bindView(R.id.frame_wrapper)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val viewPager: FrostViewPager by bindView(R.id.container)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val tabs: TabLayout by bindView(R.id.tabs)
    val appBar: AppBarLayout by bindView(R.id.appbar)
    val coordinator: CoordinatorLayout by bindView(R.id.main_content)
    override var videoViewer: FrostVideoViewer? = null
    private lateinit var drawer: Drawer
    private lateinit var drawerHeader: AccountHeader
    private var lastAccessTime = -1L

    override var searchView: SearchView? = null
    private val searchViewCache = mutableMapOf<String, List<SearchItem>>()
    private var controlWebview: WebView? = null

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = System.currentTimeMillis()
        setFrameContentView(Prefs.mainActivityLayout.layoutRes)
        setFrostColors {
            toolbar(toolbar)
            themeWindow = false
            header(appBar)
            background(viewPager)
        }
        setSupportActionBar(toolbar)
        adapter = SectionsPagerAdapter(loadFbTabs())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = TAB_COUNT
        tabs.setBackgroundColor(Prefs.mainActivityLayout.backgroundColor())
        onNestedCreate(savedInstanceState)
        L.i { "Main finished loading UI in ${System.currentTimeMillis() - start} ms" }
        controlWebview = WebView(this)
        if (BuildConfig.VERSION_CODE > Prefs.versionCode) {
            Prefs.prevVersionCode = Prefs.versionCode
            Prefs.versionCode = BuildConfig.VERSION_CODE
            if (!BuildConfig.DEBUG) {
                frostChangelog()
                frostEvent(
                    "Version",
                    "Version code" to BuildConfig.VERSION_CODE,
                    "Prev version code" to Prefs.prevVersionCode,
                    "Version name" to BuildConfig.VERSION_NAME,
                    "Build type" to BuildConfig.BUILD_TYPE,
                    "Frost id" to Prefs.frostId
                )
            }
        }
        setupDrawer(savedInstanceState)
        L.i { "Main started in ${System.currentTimeMillis() - start} ms" }
        initFab()
        lastAccessTime = System.currentTimeMillis()
    }

    /**
     * Injector to handle creation for sub classes
     */
    protected abstract fun onNestedCreate(savedInstanceState: Bundle?)

    private var hasFab = false
    private var shouldShow = false

    private fun initFab() {
        hasFab = false
        shouldShow = false
        fab.backgroundTintList = ColorStateList.valueOf(Prefs.headerColor.withMinAlpha(200))
        fab.hide()
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (!hasFab) return@OnOffsetChangedListener
            val percent = Math.abs(verticalOffset.toFloat() / appBarLayout.totalScrollRange)
            val shouldShow = percent < 0.2
            if (this.shouldShow != shouldShow) {
                this.shouldShow = shouldShow
                fab.showIf(shouldShow)
            }
        })
    }

    override fun showFab(iicon: IIcon, clickEvent: () -> Unit) {
        hasFab = true
        fab.setOnClickListener { clickEvent() }
        if (shouldShow) {
            if (fab.isShown) {
                fab.fadeScaleTransition {
                    setIcon(iicon, Prefs.iconColor)
                }
                return
            }
        }
        fab.setIcon(iicon, Prefs.iconColor)
        fab.showIf(shouldShow)
    }

    override fun hideFab() {
        hasFab = false
        fab.setOnClickListener(null)
        fab.hide()
    }

    fun tabsForEachView(action: (position: Int, view: BadgedIcon) -> Unit) {
        (0 until tabs.tabCount).asSequence().forEach { i ->
            action(i, tabs.getTabAt(i)!!.customView as BadgedIcon)
        }
    }

    private fun setupDrawer(savedInstanceState: Bundle?) {
        val navBg = Prefs.bgColor.withMinAlpha(200).toLong()
        val navHeader = Prefs.headerColor.withMinAlpha(200)
        drawer = drawer {
            toolbar = this@BaseMainActivity.toolbar
            savedInstance = savedInstanceState
            translucentStatusBar = false
            sliderBackgroundColor = navBg
            drawerHeader = accountHeader {
                textColor = Prefs.iconColor.toLong()
                backgroundDrawable = ColorDrawable(navHeader)
                selectionSecondLineShown = false
                cookies().forEach { (id, name) ->
                    profile(name = name ?: "") {
                        iconUrl = profilePictureUrl(id)
                        textColor = Prefs.textColor.toLong()
                        selectedTextColor = Prefs.textColor.toLong()
                        selectedColor = 0x00000001.toLong()
                        identifier = id
                    }
                }
                profileSetting(nameRes = R.string.kau_logout) {
                    iicon = GoogleMaterial.Icon.gmd_exit_to_app
                    iconColor = Prefs.textColor.toLong()
                    textColor = Prefs.textColor.toLong()
                    identifier = -2L
                }
                profileSetting(nameRes = R.string.kau_add_account) {
                    iconDrawable =
                        IconicsDrawable(this@BaseMainActivity, GoogleMaterial.Icon.gmd_add).actionBar().paddingDp(5)
                            .color(Prefs.textColor)
                    textColor = Prefs.textColor.toLong()
                    identifier = -3L
                }
                profileSetting(nameRes = R.string.kau_manage_account) {
                    iicon = GoogleMaterial.Icon.gmd_settings
                    iconColor = Prefs.textColor.toLong()
                    textColor = Prefs.textColor.toLong()
                    identifier = -4L
                }
                onProfileChanged { _, profile, current ->
                    if (current) launchWebOverlay(FbItem.PROFILE.url)
                    else when (profile.identifier) {
                        -2L -> {
                            val currentCookie = loadFbCookie(Prefs.userId)
                            if (currentCookie == null) {
                                toast(R.string.account_not_found)
                                FbCookie.reset { launchLogin(cookies(), true) }
                            } else {
                                materialDialogThemed {
                                    title(R.string.kau_logout)
                                    content(
                                        String.format(
                                            string(R.string.kau_logout_confirm_as_x), currentCookie.name
                                                ?: Prefs.userId.toString()
                                        )
                                    )
                                    positiveText(R.string.kau_yes)
                                    negativeText(R.string.kau_no)
                                    onPositive { _, _ -> FbCookie.logout(this@BaseMainActivity) }
                                }
                            }
                        }
                        -3L -> launchNewTask<LoginActivity>(clearStack = false)
                        -4L -> launchNewTask<SelectorActivity>(cookies(), false)
                        else -> {
                            FbCookie.switchUser(profile.identifier, this@BaseMainActivity::refreshAll)
                            tabsForEachView { _, view -> view.badgeText = null }
                        }
                    }
                    false
                }
            }
            drawerHeader.setActiveProfile(Prefs.userId)
            primaryFrostItem(FbItem.FEED_MOST_RECENT)
            primaryFrostItem(FbItem.FEED_TOP_STORIES)
            primaryFrostItem(FbItem.ACTIVITY_LOG)
            divider()
            primaryFrostItem(FbItem.PHOTOS)
            primaryFrostItem(FbItem.GROUPS)
            primaryFrostItem(FbItem.FRIENDS)
            primaryFrostItem(FbItem.CHAT)
            primaryFrostItem(FbItem.PAGES)
            divider()
            primaryFrostItem(FbItem.EVENTS)
            primaryFrostItem(FbItem.BIRTHDAYS)
            primaryFrostItem(FbItem.ON_THIS_DAY)
            divider()
            primaryFrostItem(FbItem.NOTES)
            primaryFrostItem(FbItem.SAVED)
        }
    }

    private fun Builder.primaryFrostItem(item: FbItem) = this.primaryItem(item.titleId) {
        iicon = item.icon
        iconColor = Prefs.textColor.toLong()
        textColor = Prefs.textColor.toLong()
        selectedIconColor = Prefs.textColor.toLong()
        selectedTextColor = Prefs.textColor.toLong()
        selectedColor = 0x00000001.toLong()
        identifier = item.titleId.toLong()
        onClick { _ ->
            frostEvent("Drawer Tab", "name" to item.name)
            launchWebOverlay(item.url)
            false
        }
    }

    private fun Builder.secondaryFrostItem(@StringRes title: Int, onClick: () -> Unit) = this.secondaryItem(title) {
        textColor = Prefs.textColor.toLong()
        selectedIconColor = Prefs.textColor.toLong()
        selectedTextColor = Prefs.textColor.toLong()
        selectedColor = 0x00000001.toLong()
        identifier = title.toLong()
        onClick { _ -> onClick(); false }
    }

    private fun refreshAll() {
        L.d { "Refresh all" }
        fragmentChannel.offer(REQUEST_REFRESH)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        toolbar.tint(Prefs.iconColor)
        setMenuIcons(
            menu, Prefs.iconColor,
            R.id.action_settings to GoogleMaterial.Icon.gmd_settings,
            R.id.action_search to GoogleMaterial.Icon.gmd_search
        )
        searchViewBindIfNull {
            bindSearchView(menu, R.id.action_search, Prefs.iconColor) {
                textCallback = { query, searchView ->
                    val results = searchViewCache[query]
                    if (results != null)
                        searchView.results = results
                    else {
                        val data = SearchParser.query(FbCookie.webCookie, query)?.data?.results
                        if (data != null) {
                            val items = data.mapTo(mutableListOf(), FrostSearch::toSearchItem)
                            if (items.isNotEmpty())
                                items.add(
                                    SearchItem(
                                        "${FbItem._SEARCH.url}?q=$query",
                                        string(R.string.show_all_results),
                                        iicon = null
                                    )
                                )
                            searchViewCache[query] = items
                            searchView.results = items
                        }
                    }
                }
                textDebounceInterval = 300
                searchCallback = { query, _ -> launchWebOverlay("${FbItem._SEARCH.url}/?q=$query"); true }
                closeListener = { _ -> searchViewCache.clear() }
                foregroundColor = Prefs.textColor
                backgroundColor = Prefs.bgColor.withMinAlpha(200)
                onItemClick = { _, key, _, _ -> launchWebOverlay(key) }
            }
        }
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
                val bundle =
                    ActivityOptions.makeCustomAnimation(this, R.anim.kau_slide_in_right, R.anim.kau_fade_out).toBundle()
                startActivityForResult(intent, ACTIVITY_SETTINGS, bundle)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun openFileChooser(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ) {
        openMediaPicker(filePathCallback, fileChooserParams)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (onActivityResultWeb(requestCode, resultCode, data)) return
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_SETTINGS) {
            if (resultCode and REQUEST_RESTART_APPLICATION > 0) { //completely restart application
                L.d { "Restart Application Requested" }
                val intent = packageManager.getLaunchIntentForPackage(packageName)!!
                Intent.makeRestartActivityTask(intent.component)
                Runtime.getRuntime().exit(0)
                return
            }
            if (resultCode and REQUEST_RESTART > 0) return restart()
            /*
             * These results can be stacked
             */
            if (resultCode and REQUEST_REFRESH > 0) fragmentChannel.offer(REQUEST_REFRESH)
            if (resultCode and REQUEST_NAV > 0) frostNavigationBar()
            if (resultCode and REQUEST_TEXT_ZOOM > 0) fragmentChannel.offer(REQUEST_TEXT_ZOOM)
            if (resultCode and REQUEST_SEARCH > 0) invalidateOptionsMenu()
        }
    }

    private val STATE_FORCE_FALLBACK = "frost_state_force_fallback"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(STATE_FORCE_FALLBACK, ArrayList(adapter.forcedFallbacks))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        adapter.forcedFallbacks.clear()
        adapter.forcedFallbacks.addAll(
            savedInstanceState.getStringArrayList(STATE_FORCE_FALLBACK)
                ?: emptyList()
        )
    }

    override fun onResume() {
        super.onResume()
        FbCookie.switchBackUser {}
        controlWebview?.resumeTimers()
        if (System.currentTimeMillis() - lastAccessTime > MAIN_TIMEOUT_DURATION) {
            refreshAll()
        }
        lastAccessTime = System.currentTimeMillis() // precaution to avoid loops
    }

    override fun onPause() {
        controlWebview?.pauseTimers()
        L.v { "Pause main web timers" }
        lastAccessTime = System.currentTimeMillis()
        super.onPause()
    }

    override fun onDestroy() {
        controlWebview?.destroy()
        super.onDestroy()
    }

    override fun collapseAppBar() {
        appBar.post { appBar.setExpanded(false) }
    }

    override fun backConsumer(): Boolean {
        if (currentFragment.onBackPressed()) return true
        if (Prefs.exitConfirmation) {
            materialDialogThemed {
                title(R.string.kau_exit)
                content(R.string.kau_exit_confirmation)
                positiveText(R.string.kau_yes)
                negativeText(R.string.kau_no)
                onPositive { _, _ -> finish() }
                checkBoxPromptRes(R.string.kau_do_not_show_again, false) { _, b -> Prefs.exitConfirmation = !b }
            }
            return true
        }
        return false
    }

    inline val currentFragment
        get() = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.container}:${viewPager.currentItem}") as BaseFragment

    override fun reloadFragment(fragment: BaseFragment) {
        runOnUiThread { adapter.reloadFragment(fragment) }
    }

    inner class SectionsPagerAdapter(val pages: List<FbItem>) : FragmentPagerAdapter(supportFragmentManager) {

        val forcedFallbacks = mutableSetOf<String>()

        fun reloadFragment(fragment: BaseFragment) {
            if (fragment is WebFragment) return
            L.d { "Reload fragment ${fragment.position}: ${fragment.baseEnum.name}" }
            forcedFallbacks.add(fragment.baseEnum.name)
            supportFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
            notifyDataSetChanged()
        }

        override fun getItem(position: Int): Fragment {
            val item = pages[position]
            return BaseFragment(
                item.fragmentCreator,
                forcedFallbacks.contains(item.name),
                item,
                position
            )
        }

        override fun getCount() = pages.size

        override fun getPageTitle(position: Int): CharSequence = getString(pages[position].titleId)

        override fun getItemPosition(fragment: Any) =
            if (fragment !is BaseFragment)
                POSITION_UNCHANGED
            else if (fragment is WebFragment || fragment.valid)
                POSITION_UNCHANGED
            else
                POSITION_NONE
    }

    override val lowerVideoPadding: PointF
        get() =
            if (Prefs.mainActivityLayout == MainActivityLayout.BOTTOM_BAR)
                PointF(0f, toolbar.height.toFloat())
            else
                PointF(0f, 0f)
}
