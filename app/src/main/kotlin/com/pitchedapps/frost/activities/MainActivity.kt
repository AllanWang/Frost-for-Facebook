package com.pitchedapps.frost.activities

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.views.BadgedIcon
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class MainActivity : BaseMainActivity() {

    override val fragmentSubject = PublishSubject.create<Int>()!!
    var lastPosition = -1
    val headerBadgeObservable = PublishSubject.create<String>()!!

    override fun onNestedCreate(savedInstanceState: Bundle?) {
        setupTabs()
        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (lastPosition == position) return
                if (lastPosition != -1) fragmentSubject.onNext(-(lastPosition + 1))
                fragmentSubject.onNext(position)
                lastPosition = position
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                val delta = positionOffset * (255 - 128).toFloat()
                tabsForEachView { tabPosition, view ->
                    view.setAllAlpha(when (tabPosition) {
                        position -> 255.0f - delta
                        position + 1 -> 128.0f + delta
                        else -> 128f
                    })
                }
            }
        })
        viewPager.post { fragmentSubject.onNext(0); lastPosition = 0 } //trigger hook so title is set

    }


    private fun setupTabs() {
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                currentFragment.onTabClick()
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                super.onTabSelected(tab)
                (tab.customView as BadgedIcon).badgeText = null
            }
        })
        headerBadgeObservable.throttleFirst(15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .map { Jsoup.parse(it) }
                .filter { it.select("[data-sigil=count]").size >= 0 } //ensure headers exist
                .map {
                    val feed = it.select("[data-sigil*=feed] [data-sigil=count]")
                    val requests = it.select("[data-sigil*=requests] [data-sigil=count]")
                    val messages = it.select("[data-sigil*=messages] [data-sigil=count]")
                    val notifications = it.select("[data-sigil*=notifications] [data-sigil=count]")
                    return@map arrayOf(feed, requests, messages, notifications).map { it?.getOrNull(0)?.ownText() }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (feed, requests, messages, notifications) ->
                    tabsForEachView { _, view ->
                        when (view.iicon) {
                            FbItem.FEED.icon -> view.badgeText = feed
                            FbItem.FRIENDS.icon -> view.badgeText = requests
                            FbItem.MESSAGES.icon -> view.badgeText = messages
                            FbItem.NOTIFICATIONS.icon -> view.badgeText = notifications
                        }
                    }
                }
        adapter.pages.forEach {
            tabs.addTab(tabs.newTab()
                    .setCustomView(BadgedIcon(this).apply { iicon = it.icon }))
        }
    }

}
