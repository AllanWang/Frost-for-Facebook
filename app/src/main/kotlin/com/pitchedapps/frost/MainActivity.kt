package com.pitchedapps.frost

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import ca.allanwang.kau.utils.*
import co.zsmb.materialdrawerkt.builders.Builder
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import co.zsmb.materialdrawerkt.draweritems.profile.profileSetting
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.pitchedapps.frost.dbflow.loadFbTabs
import com.pitchedapps.frost.dbflow.saveAsync
import com.pitchedapps.frost.facebook.FbCookie.switchUser
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class MainActivity : BaseActivity() {

    lateinit var adapter: SectionsPagerAdapter
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val viewPager: ViewPager by bindView(R.id.container)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val tabs: TabLayout by bindView(R.id.tabs)
    lateinit var drawer: Drawer
    lateinit var drawerHeader: AccountHeader
    var titleDisposable: Disposable? = null
    var refreshObservable = PublishSubject.create<Boolean>()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        adapter = SectionsPagerAdapter(supportFragmentManager, loadFbTabs())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTitleListener()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                val delta: Float by lazy { positionOffset * (255 - 128).toFloat() }
                (0 until tabs.tabCount).asSequence().forEach {
                    i ->
                    tabs.getTabAt(i)?.icon?.alpha = when (i) {
                        position -> (255.0 - delta).toInt()
                        position + 1 -> (128.0 + delta).toInt()
                        else -> 128
                    }
                }
            }
        })
        setupDrawer(savedInstanceState)
        setupTabs()
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        viewPager.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateTitleListener()
            }
        })
    }

    fun updateTitleListener() {
        titleDisposable?.dispose()
        titleDisposable = currentFragment.web.addTitleListener({ toolbar.title = it })
    }

    fun setupTabs() {
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                currentFragment.web.scrollOrRefresh()
            }
        })
        adapter.pages.forEach { tabs.addTab(tabs.newTab().setIcon(it.icon.toDrawable(this))) }
    }

    fun setupDrawer(savedInstanceState: Bundle?) {
        drawer = drawer {
            toolbar = this@MainActivity.toolbar
            savedInstance = savedInstanceState
            translucentStatusBar = false
            sliderBackgroundColor = Prefs.bgColor.withMinAlpha(200).toLong()
            drawerHeader = accountHeader {
                textColor = Prefs.textColor.toLong()
                backgroundDrawable = ColorDrawable(Prefs.headerColor)
                cookies().forEach { (id, name) ->
                    profile(name = name ?: "") {
                        iconUrl = PROFILE_PICTURE_URL(id)
                        textColor = Prefs.textColor.toLong()
                        selectedTextColor = Prefs.textColor.toLong()
                        identifier = id
                    }
                }
                profileSetting(nameRes = R.string.add_account) {
                    iconDrawable = IconicsDrawable(this@MainActivity, GoogleMaterial.Icon.gmd_add).actionBar().paddingDp(5).color(Prefs.textColor)
                    textColor = Prefs.textColor.toLong()
                    identifier = -2L
                }
                profileSetting(nameRes = R.string.manage_account) {
                    iicon = GoogleMaterial.Icon.gmd_settings
                    iconColor = Prefs.textColor.toLong()
                    textColor = Prefs.textColor.toLong()
                    identifier = -3L
                }
                onProfileChanged { _, profile, current ->
                    if (current) launchWebOverlay(FbTab.PROFILE.url)
                    else when (profile.identifier) {
                        -2L -> launchNewTask(LoginActivity::class.java, clearStack = false)
                        -3L -> launchNewTask(SelectorActivity::class.java, cookies(), false)
                        else -> switchUser(profile.identifier, { refreshObservable.onNext(true) })
                    }
                    false
                }
            }
            drawerHeader.setActiveProfile(Prefs.userId)
            primaryItem(FbTab.ACTIVITY_LOG)
            primaryItem(FbTab.PHOTOS)
            primaryItem(FbTab.GROUPS)
        }
    }

    fun Builder.primaryItem(item: FbTab) = this.primaryItem(item.titleId) {
        iicon = item.icon
        iconColor = Prefs.textColor.toLong()
        textColor = Prefs.textColor.toLong()
        selectedIconColor = Prefs.textColor.toLong()
        selectedTextColor = Prefs.textColor.toLong()
        selectedColor = 0x00000001.toLong()
        identifier = item.titleId.toLong()
        onClick { _ ->
            launchWebOverlay(item.url)
            false
        }
    }

    fun refreshAll() {
        refreshObservable.onNext(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivitySlideIn(SettingsActivity::class.java, clearStack = true, intentBuilder = {
                    putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
                })
            }
            R.id.action_changelog -> showChangelog(R.xml.changelog)
            R.id.action_call -> launchNewTask(LoginActivity::class.java)
            R.id.action_db -> adapter.pages.saveAsync(this)
            R.id.action_restart -> restart()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (currentFragment.onBackPressed()) return
        super.onBackPressed()
    }

    val currentFragment
        get() = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.container}:${viewPager.currentItem}") as WebFragment

    inner class SectionsPagerAdapter(fm: FragmentManager, val pages: List<FbTab>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int) = WebFragment(pages[position])

        override fun getCount() = pages.size

        override fun getPageTitle(position: Int): CharSequence = getString(pages[position].titleId)
    }

}
