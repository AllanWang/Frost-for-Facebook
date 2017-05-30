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
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.loadFbTab
import com.pitchedapps.frost.fragments.BaseFragment
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.Changelog
import com.pitchedapps.frost.utils.KeyPairObservable
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.toDrawable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class MainActivity : AppCompatActivity(), KeyPairObservable {

    override val observable: Subject<Pair<Int, Int>> = PublishSubject.create<Pair<Int, Int>>()

    lateinit var adapter: SectionsPagerAdapter
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val viewPager: ViewPager by bindView(R.id.container)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val tabs: TabLayout by bindView(R.id.tabs)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        adapter = SectionsPagerAdapter(supportFragmentManager, loadFbTab(this@MainActivity))
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 5
        setupTabs()
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    fun setupTabs() {

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
//        tabs.setupWithViewPager(viewPager)
        adapter.pages.forEach { tabs.addTab(tabs.newTab().setIcon(it.icon.toDrawable(this))) }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.action_changelog -> Changelog.show(this)
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

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, val pages: List<FbTab>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int) = WebFragment.newInstance(position, pages[position].url)

        override fun getCount() = pages.size

        override fun getPageTitle(position: Int): CharSequence = pages[position].title
    }
}
