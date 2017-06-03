package com.pitchedapps.frost

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import butterknife.ButterKnife
import co.zsmb.materialdrawerkt.builders.Builder
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbTabs
import com.pitchedapps.frost.dbflow.saveAsync
import com.pitchedapps.frost.events.FbAccountEvent
import com.pitchedapps.frost.facebook.FbCookie.switchUser
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.PROFILE_PICTURE_URL
import com.pitchedapps.frost.fragments.BaseFragment
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    lateinit var adapter: SectionsPagerAdapter
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val viewPager: ViewPager by bindView(R.id.container)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val tabs: TabLayout by bindView(R.id.tabs)
    lateinit var drawer: Drawer
    lateinit var drawerHeader: AccountHeader
    val cookies = cookies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        adapter = SectionsPagerAdapter(supportFragmentManager, loadFbTabs())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
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
        setupTabs()
        setupDrawer(savedInstanceState)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    fun setupTabs() {
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        adapter.pages.forEach { tabs.addTab(tabs.newTab().setIcon(it.icon.toDrawable(this))) }
    }

    fun setupDrawer(savedInstanceState: Bundle?) {
        drawer = drawer {
            toolbar = this@MainActivity.toolbar
            savedInstance = savedInstanceState
            translucentStatusBar = false
            drawerHeader = accountHeader {
                cookies.forEach {
                    profile(name = it.name ?: "") {
                        iconUrl = PROFILE_PICTURE_URL(it.id)
                    }
                }
                onProfileChanged { _, profile, current ->
                    if (current) WebOverlayActivity.newInstance(this@MainActivity, FbTab.PROFILE)
                    else switchUser(profile.name.text)
                    false
                }
            }
//            profile("a") {
//
//            }
//            if (Prefs.userId != Prefs.userIdDefault) {
//                profile("a")
//            }
            primaryItem(FbTab.ACTIVITY_LOG)
            primaryItem(FbTab.PHOTOS)
            primaryItem(FbTab.GROUPS)
        }
    }

    fun Builder.primaryItem(item: FbTab) = this.primaryItem(item.titleId) {
        iicon = item.icon
        identifier = item.titleId.toLong()
        onClick { _ ->
            launchWebOverlay(item.url)
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
            }
            R.id.action_changelog -> Changelog.show(this)
            R.id.action_call -> launchNewTask(LoginActivity::class.java)
            R.id.action_db -> adapter.pages.saveAsync(this)
            R.id.action_restart -> {
                finish();
                overridePendingTransition(0, 0); //No transitions
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (currentFragment.onBackPressed()) return
        super.onBackPressed()
    }

    val currentFragment: BaseFragment
        get() = supportFragmentManager.findFragmentByTag("android:switcher:${R.id.container}:${viewPager.currentItem}") as BaseFragment

    inner class SectionsPagerAdapter(fm: FragmentManager, val pages: List<FbTab>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int) = WebFragment.newInstance(position, pages[position].url)

        override fun getCount() = pages.size

        override fun getPageTitle(position: Int): CharSequence = getString(pages[position].titleId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun accountEvent(event: FbAccountEvent) = event.execute(drawerHeader)

    override fun onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
