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
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PointF
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ca.allanwang.kau.searchview.SearchItem
import ca.allanwang.kau.searchview.SearchView
import ca.allanwang.kau.searchview.SearchViewHolder
import ca.allanwang.kau.searchview.bindSearchView
import ca.allanwang.kau.ui.ProgressAnimator
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.fadeScaleTransition
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.invisible
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.setMenuIcons
import ca.allanwang.kau.utils.showIf
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toDrawable
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withMinAlpha
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.FileChooserContract
import com.pitchedapps.frost.contracts.FileChooserDelegate
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.databinding.ActivityMainBinding
import com.pitchedapps.frost.databinding.ActivityMainBottomTabsBinding
import com.pitchedapps.frost.databinding.ActivityMainDrawerWrapperBinding
import com.pitchedapps.frost.databinding.ViewNavHeaderBinding
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.db.GenericDao
import com.pitchedapps.frost.db.getTabs
import com.pitchedapps.frost.enums.MainActivityLayout
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostSearch
import com.pitchedapps.frost.facebook.parsers.SearchParser
import com.pitchedapps.frost.facebook.profilePictureUrl
import com.pitchedapps.frost.fragments.BaseFragment
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.services.scheduleNotificationsFromPrefs
import com.pitchedapps.frost.utils.ACTIVITY_SETTINGS
import com.pitchedapps.frost.utils.BiometricUtils
import com.pitchedapps.frost.utils.EXTRA_COOKIES
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.MAIN_TIMEOUT_DURATION
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_FAB
import com.pitchedapps.frost.utils.REQUEST_NAV
import com.pitchedapps.frost.utils.REQUEST_NOTIFICATION
import com.pitchedapps.frost.utils.REQUEST_REFRESH
import com.pitchedapps.frost.utils.REQUEST_RESTART
import com.pitchedapps.frost.utils.REQUEST_RESTART_APPLICATION
import com.pitchedapps.frost.utils.REQUEST_SEARCH
import com.pitchedapps.frost.utils.REQUEST_TEXT_ZOOM
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.frostChangelog
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.frostNavigationBar
import com.pitchedapps.frost.utils.launchWebOverlay
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.views.BadgedIcon
import com.pitchedapps.frost.views.FrostVideoViewer
import com.pitchedapps.frost.views.FrostViewPager
import com.pitchedapps.frost.widgets.NotificationWidget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.abs

/**
 * Created by Allan Wang on 20/12/17.
 *
 * Most of the logic that is unrelated to handling fragments
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
abstract class BaseMainActivity : BaseActivity(), MainActivityContract,
    FileChooserContract by FileChooserDelegate(),
    VideoViewHolder, SearchViewHolder {

    /**
     * Note that tabs themselves are initialized through a coroutine during onCreate
     */
    protected val adapter: SectionsPagerAdapter = SectionsPagerAdapter()
    override val frameWrapper: FrameLayout get() = drawerWrapperBinding.mainContainer
    lateinit var drawerWrapperBinding: ActivityMainDrawerWrapperBinding
    lateinit var contentBinding: ActivityMainContentBinding
    val cookieDao: CookieDao by inject()
    val genericDao: GenericDao by inject()

    interface ActivityMainContentBinding {
        val root: View
        val toolbar: Toolbar
        val viewpager: FrostViewPager
        val tabs: TabLayout
        val appbar: AppBarLayout
        val fab: FloatingActionButton
    }

    protected var lastPosition = -1

    override var videoViewer: FrostVideoViewer? = null
    private var lastAccessTime = -1L

    override var searchView: SearchView? = null
    private val searchViewCache = mutableMapOf<String, List<SearchItem>>()
    private var controlWebview: WebView? = null

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val start = System.currentTimeMillis()
        drawerWrapperBinding = ActivityMainDrawerWrapperBinding.inflate(layoutInflater)
        setContentView(drawerWrapperBinding.root)
        contentBinding = when (Prefs.mainActivityLayout) {
            MainActivityLayout.TOP_BAR -> {
                val binding = ActivityMainBinding.inflate(layoutInflater)
                object : ActivityMainContentBinding {
                    override val root: View = binding.root
                    override val toolbar: Toolbar = binding.toolbar
                    override val viewpager: FrostViewPager = binding.viewpager
                    override val tabs: TabLayout = binding.tabs
                    override val appbar: AppBarLayout = binding.appbar
                    override val fab: FloatingActionButton = binding.fab
                }
            }
            MainActivityLayout.BOTTOM_BAR -> {
                val binding = ActivityMainBottomTabsBinding.inflate(layoutInflater)
                object : ActivityMainContentBinding {
                    override val root: View = binding.root
                    override val toolbar: Toolbar = binding.toolbar
                    override val viewpager: FrostViewPager = binding.viewpager
                    override val tabs: TabLayout = binding.tabs
                    override val appbar: AppBarLayout = binding.appbar
                    override val fab: FloatingActionButton = binding.fab
                }
            }
        }
        drawerWrapperBinding.mainContainer.addView(contentBinding.root)
        with(contentBinding) {
            setFrostColors {
                toolbar(toolbar)
                themeWindow = false
                header(appbar)
                background(viewpager)
            }
            setSupportActionBar(toolbar)
            viewpager.adapter = adapter
            tabs.setBackgroundColor(Prefs.mainActivityLayout.backgroundColor())

        }
        onNestedCreate(savedInstanceState)
        L.i { "Main finished loading UI in ${System.currentTimeMillis() - start} ms" }
        launch {
            adapter.setPages(genericDao.getTabs())
        }
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
//        setupDrawer(savedInstanceState)
        L.i { "Main started in ${System.currentTimeMillis() - start} ms" }
        drawerWrapperBinding.initDrawer()
        contentBinding.initFab()
        lastAccessTime = System.currentTimeMillis()
    }

    /**
     * Injector to handle creation for sub classes
     */
    protected abstract fun onNestedCreate(savedInstanceState: Bundle?)

    private var hasFab = false
    private var shouldShow = false

    private class FrostMenuBuilder(private val context: Context, private val menu: Menu) {
        private var order: Int = 0
        private var groupId: Int = 13
        private val items: MutableList<Menu> = mutableListOf()

        fun primaryFrostItem(fbItem: FbItem) {
            val item = menu.add(groupId, fbItem.ordinal, order++, context.string(fbItem.titleId))
            item.icon = fbItem.icon.toDrawable(context, 18)
        }

        fun divider() {
            groupId++
        }

        fun secondaryFrostItem(fbItem: FbItem) {
            menu.add(groupId, fbItem.ordinal, order++, context.string(fbItem.titleId))
        }
    }

    private fun ActivityMainDrawerWrapperBinding.initDrawer() {

        fun createNavDrawable(@ColorInt foregroundColor: Int): RippleDrawable {
            val drawable = drawable(R.drawable.nav_item_background) as RippleDrawable
            drawable.setColor(
                ColorStateList(
                    arrayOf(intArrayOf()),
                    intArrayOf(foregroundColor.adjustAlpha(0.16f))
                )
            )
            return drawable
        }

        val toggle = ActionBarDrawerToggle(
            this@BaseMainActivity, drawer, contentBinding.toolbar,
            R.string.open,
            R.string.close
        )
        toggle.isDrawerSlideAnimationEnabled = false
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val foregroundColor = ColorStateList.valueOf(Prefs.textColor)

        with(navigation) {
            FrostMenuBuilder(this@BaseMainActivity, menu).apply {
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
                primaryFrostItem(FbItem.MARKETPLACE)
            }
            setNavigationItemSelectedListener {
                val item = FbItem.values[it.itemId]
                frostEvent("Drawer Tab", "name" to item.name)
                drawer.closeDrawer(navigation)
                launchWebOverlay(item.url)
                false
            }
            val navBg = Prefs.bgColor.withMinAlpha(200)
            setBackgroundColor(navBg)
            itemBackground = createNavDrawable(Prefs.accentColor)
            itemTextColor = foregroundColor
            itemIconTintList = foregroundColor

            val header = NavHeader()
            addHeaderView(header.root)
        }
    }

    private fun ActivityMainContentBinding.initFab() {
        hasFab = false
        shouldShow = false
        fab.backgroundTintList = ColorStateList.valueOf(Prefs.headerColor.withMinAlpha(200))
        fab.hide()
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (!hasFab) return@OnOffsetChangedListener
            val percent = abs(verticalOffset.toFloat() / appBarLayout.totalScrollRange)
            val shouldShow = percent < 0.2
            if (this@BaseMainActivity.shouldShow != shouldShow) {
                this@BaseMainActivity.shouldShow = shouldShow
                fab.showIf(shouldShow)
            }
        })
    }

    override fun showFab(iicon: IIcon, clickEvent: () -> Unit) {
        with(contentBinding) {
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
    }

    override fun hideFab() {
        with(contentBinding) {
            hasFab = false
            fab.setOnClickListener(null)
            fab.hide()
        }
    }

    fun tabsForEachView(action: (position: Int, view: BadgedIcon) -> Unit) {
        with(contentBinding) {
            (0 until tabs.tabCount).asSequence().forEach { i ->
                action(i, tabs.getTabAt(i)!!.customView as BadgedIcon)
            }
        }
    }

//    private fun setupDrawer(savedInstanceState: Bundle?) {
//        val navBg = Prefs.bgColor.withMinAlpha(200).toLong()
//        val navHeader = Prefs.headerColor.withMinAlpha(200)
//        drawer = drawer {
//            toolbar = this@BaseMainActivity.toolbar
//            savedInstance = savedInstanceState
//            translucentStatusBar = false
//            sliderBackgroundColor = navBg
//            drawerHeader = accountHeader {
//                textColor = Prefs.iconColor.toLong()
//                backgroundDrawable = ColorDrawable(navHeader)
//                selectionSecondLineShown = false
//                cookies().forEach { (id, name) ->
//                    profile(name = name ?: "") {
//                        iconUrl = profilePictureUrl(id)
//                        textColor = Prefs.textColor.toLong()
//                        selectedTextColor = Prefs.textColor.toLong()
//                        selectedColor = 0x00000001.toLong()
//                        identifier = id
//                    }
//                }
//                profileSetting(nameRes = R.string.kau_logout) {
//                    iicon = GoogleMaterial.Icon.gmd_exit_to_app
//                    iconColor = Prefs.textColor.toLong()
//                    textColor = Prefs.textColor.toLong()
//                    identifier = -2L
//                }
//                profileSetting(nameRes = R.string.kau_add_account) {
//                    iconDrawable =
//                        IconicsDrawable(
//                            this@BaseMainActivity,
//                            GoogleMaterial.Icon.gmd_add
//                        ).actionBar().paddingDp(5)
//                            .colorInt(Prefs.textColor)
//                    textColor = Prefs.textColor.toLong()
//                    identifier = -3L
//                }
//                profileSetting(nameRes = R.string.kau_manage_account) {
//                    iicon = GoogleMaterial.Icon.gmd_settings
//                    iconColor = Prefs.textColor.toLong()
//                    textColor = Prefs.textColor.toLong()
//                    identifier = -4L
//                }
//                onProfileChanged { _, profile, current ->
//                    if (current) launchWebOverlay(FbItem.PROFILE.url)
//                    else when (profile.identifier) {
//                        -2L -> {
//                            // TODO no backpressure support
//                            this@BaseMainActivity.launch {
//                                val currentCookie = cookieDao.currentCookie()
//                                if (currentCookie == null) {
//                                    toast(R.string.account_not_found)
//                                    FbCookie.reset()
//                                    launchLogin(cookies(), true)
//                                } else {
//                                    materialDialog {
//                                        title(R.string.kau_logout)
//                                        message(
//                                            text =
//                                            String.format(
//                                                string(R.string.kau_logout_confirm_as_x),
//                                                currentCookie.name ?: Prefs.userId.toString()
//                                            )
//                                        )
//                                        positiveButton(R.string.kau_yes) {
//                                            this@BaseMainActivity.launch {
//                                                FbCookie.logout(this@BaseMainActivity)
//                                            }
//                                        }
//                                        negativeButton(R.string.kau_no)
//                                    }
//                                }
//                            }
//                        }
//                        -3L -> launchNewTask<LoginActivity>(clearStack = false)
//                        -4L -> launchNewTask<SelectorActivity>(cookies(), false)
//                        else -> {
//                            this@BaseMainActivity.launch {
//                                FbCookie.switchUser(profile.identifier)
//                                tabsForEachView { _, view -> view.badgeText = null }
//                                refreshAll()
//                            }
//                        }
//                    }
//                    false
//                }
//            }

    private inner class NavHeader {

        private var orderedAccounts: List<CookieEntity> = cookies()
        private var pendingUpdate: Boolean = false
        private val binding = ViewNavHeaderBinding.inflate(layoutInflater)
        val root: View get() = binding.root

        init {
            setPrimary(Prefs.userId)
            binding.updateAccounts()
            with(drawerWrapperBinding) {
                drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                    override fun onDrawerClosed(drawerView: View) {
                        if (drawer !== navigation) return
                        if (!pendingUpdate) return
                        pendingUpdate = false
                        binding.updateAccounts()
                    }
                })
            }
            with(binding) {
                optionsContainer.setBackgroundColor(
                    Prefs.bgColor.withMinAlpha(200).colorToForeground(
                        0.1f
                    )
                )
                var showOptions = false
                val animator: ProgressAnimator = ProgressAnimator.ofFloat {  }
                background.setOnClickListener {
                    animator.reset()
                    if (showOptions) {
                        animator.apply {
                            withAnimator(optionsContainer.height.toFloat(), 0f) {
                                optionsContainer.updateLayoutParams {
                                    height = it.toInt()
                                }
                            }
                            withEndAction {
                                optionsContainer.gone()
                            }
                        }
                    } else {
                        animator.apply {
                            optionsContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                            withAnimator(
                                optionsContainer.height.toFloat(),
                                optionsContainer.measuredHeight.toFloat()
                            ) {
                                optionsContainer.updateLayoutParams {
                                    height = it.toInt()
                                }
                            }
                            withStartAction {
                                optionsContainer.visible()
                            }
                        }
                    }
                    showOptions = !showOptions
                    animator.start()
                }
            }
        }

        private fun setPrimary(id: Long) {
            val (primaries, others) = orderedAccounts.partition { it.id == id }
            if (primaries.size != 1) {
                L._e(null) { "Updating account primaries, could not find specified id" }
            }
            orderedAccounts = primaries + others
        }

        /**
         * Syncs UI to match [orderedAccounts].
         *
         * We keep this separate as we usually only want to update when the drawer is hidden.
         */
        private fun ViewNavHeaderBinding.updateAccounts() {
            avatarPrimary.setAccount(orderedAccounts.getOrNull(0), true)
            avatarSecondary.setAccount(orderedAccounts.getOrNull(1), false)
            avatarTertiary.setAccount(orderedAccounts.getOrNull(2), false)
        }

        private fun closeDrawer() {
            with(drawerWrapperBinding) {
                drawer.closeDrawer(navigation)
            }
        }

        private fun ImageView.setAccount(
            cookie: CookieEntity?,
            primary: Boolean
        ) {
            if (cookie == null) {
                invisible()
                setOnClickListener(null)
            } else {
                visible()
                GlideApp.with(this)
                    .load(profilePictureUrl(cookie.id))
                    .transform(FrostGlide.circleCrop)
                    .into(this)
                setOnClickListener {
                    if (primary) {
                        launchWebOverlay(FbItem.PROFILE.url)
                    } else {
                        setPrimary(cookie.id)
                        pendingUpdate = true
                        closeDrawer()
                        launch {
                            FbCookie.switchUser(cookie.id)
                            tabsForEachView { _, view -> view.badgeText = null }
                            refreshAll()
                        }
                    }
                    closeDrawer()
                }
            }
        }
    }

    private fun refreshAll() {
        L.d { "Refresh all" }
        fragmentChannel.offer(REQUEST_REFRESH)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        contentBinding.toolbar.tint(Prefs.iconColor)
        setMenuIcons(
            menu, Prefs.iconColor,
            R.id.action_settings to GoogleMaterial.Icon.gmd_settings,
            R.id.action_search to GoogleMaterial.Icon.gmd_search
        )
        bindSearchView(menu)
        return true
    }

    private fun bindSearchView(menu: Menu) {
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
                searchCallback =
                    { query, _ -> launchWebOverlay("${FbItem._SEARCH.url}/?q=$query"); true }
                closeListener = { _ -> searchViewCache.clear() }
                foregroundColor = Prefs.textColor
                backgroundColor = Prefs.bgColor.withMinAlpha(200)
                onItemClick = { _, key, _, _ -> launchWebOverlay(key) }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
                val bundle =
                    ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.kau_slide_in_right,
                        R.anim.kau_fade_out
                    ).toBundle()
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

        fun hasRequest(flag: Int) = resultCode and flag > 0

        if (requestCode == ACTIVITY_SETTINGS) {
            if (resultCode and REQUEST_RESTART_APPLICATION > 0) { // completely restart application
                L.d { "Restart Application Requested" }
                val intent = packageManager.getLaunchIntentForPackage(packageName)!!
                Intent.makeRestartActivityTask(intent.component)
                Runtime.getRuntime().exit(0)
                return
            }
            if (resultCode and REQUEST_RESTART > 0) {
                NotificationWidget.forceUpdate(this)
                restart()
                return
            }
            /*
             * These results can be stacked
             */
            if (hasRequest(REQUEST_REFRESH)) {
                fragmentChannel.offer(REQUEST_REFRESH)
            }
            if (hasRequest(REQUEST_NAV)) {
                frostNavigationBar()
            }
            if (hasRequest(REQUEST_TEXT_ZOOM)) {
                fragmentChannel.offer(REQUEST_TEXT_ZOOM)
            }
            if (hasRequest(REQUEST_SEARCH)) {
                invalidateOptionsMenu()
            }
            if (hasRequest(REQUEST_FAB)) {
                fragmentChannel.offer(lastPosition)
            }
            if (hasRequest(REQUEST_NOTIFICATION)) {
                scheduleNotificationsFromPrefs()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        adapter.restoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val shouldReload = System.currentTimeMillis() - lastAccessTime > MAIN_TIMEOUT_DURATION
        lastAccessTime = System.currentTimeMillis() // precaution to avoid loops
        controlWebview?.resumeTimers()
        launch {
            val authDefer = BiometricUtils.authenticate(this@BaseMainActivity)
            FbCookie.switchBackUser()
            authDefer.await()
            if (shouldReload && Prefs.autoRefreshFeed) {
                refreshAll()
            }
        }
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
        fragmentChannel.close()
    }

    override fun collapseAppBar() {
        with(contentBinding) {
            appbar.post { appbar.setExpanded(false) }
        }
    }

    override fun backConsumer(): Boolean {
        with(drawerWrapperBinding) {
            if (drawer.isDrawerOpen(navigation)) {
                drawer.closeDrawer(navigation)
                return true
            }
        }
        if (currentFragment.onBackPressed()) return true
        if (Prefs.exitConfirmation) {
            materialDialog {
                title(R.string.kau_exit)
                message(R.string.kau_exit_confirmation)
                positiveButton(R.string.kau_yes) { finish() }
                negativeButton(R.string.kau_no)
                checkBoxPrompt(R.string.kau_do_not_show_again, isCheckedDefault = false) {
                    Prefs.exitConfirmation = !it
                }
            }
            return true
        }
        return false
    }

    inline val currentFragment
        get() = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.container}:${contentBinding.viewpager.currentItem}") as BaseFragment

    override fun reloadFragment(fragment: BaseFragment) {
        runOnUiThread { adapter.reloadFragment(fragment) }
    }

    inner class SectionsPagerAdapter : FragmentPagerAdapter(supportFragmentManager) {

        private val pages: MutableList<FbItem> = mutableListOf()

        private val forcedFallbacks = mutableSetOf<String>()

        /**
         * Update page list and prompt reload
         */
        fun setPages(pages: List<FbItem>) {
            this.pages.clear()
            this.pages.addAll(pages)
            notifyDataSetChanged()
            with(contentBinding) {
                tabs.removeAllTabs()
                this@SectionsPagerAdapter.pages.forEachIndexed { index, fbItem ->
                    tabs.addTab(
                        tabs.newTab()
                            .setCustomView(BadgedIcon(this@BaseMainActivity).apply {
                                iicon = fbItem.icon
                            }.also {
                                it.setAllAlpha(if (index == 0) SELECTED_TAB_ALPHA else UNSELECTED_TAB_ALPHA)
                            })
                    )
                }
                lastPosition = 0
                viewpager.setCurrentItem(0, false)
                viewpager.offscreenPageLimit = pages.size
                viewpager.post {
                    if (!fragmentChannel.isClosedForSend) {
                        fragmentChannel.offer(0)
                    }
                } // trigger hook so title is set
            }
        }

        fun saveInstanceState(outState: Bundle) {
            outState.putStringArrayList(STATE_FORCE_FALLBACK, ArrayList(forcedFallbacks))
        }

        fun restoreInstanceState(savedInstanceState: Bundle) {
            forcedFallbacks.clear()
            forcedFallbacks.addAll(
                savedInstanceState.getStringArrayList(STATE_FORCE_FALLBACK)
                    ?: emptyList()
            )
        }

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
            when {
                fragment !is BaseFragment -> POSITION_UNCHANGED
                fragment is WebFragment || fragment.valid -> POSITION_UNCHANGED
                else -> POSITION_NONE
            }
    }

    private val lowerVideoPaddingPointF = PointF()

    override val lowerVideoPadding: PointF
        get() {
            if (Prefs.mainActivityLayout == MainActivityLayout.BOTTOM_BAR)
                lowerVideoPaddingPointF.set(0f, contentBinding.toolbar.height.toFloat())
            else
                lowerVideoPaddingPointF.set(0f, 0f)
            return lowerVideoPaddingPointF
        }

    companion object {
        private const val STATE_FORCE_FALLBACK = "frost_state_force_fallback"
        const val SELECTED_TAB_ALPHA = 255f
        const val UNSELECTED_TAB_ALPHA = 128f
    }
}
