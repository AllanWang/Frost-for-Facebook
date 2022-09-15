/*
 * Copyright 2020 Allan Wang
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
package com.pitchedapps.frost.prefs

import android.content.Context
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryAndroid
import com.pitchedapps.frost.prefs.sections.BehaviourPrefs
import com.pitchedapps.frost.prefs.sections.BehaviourPrefsImpl
import com.pitchedapps.frost.prefs.sections.CorePrefs
import com.pitchedapps.frost.prefs.sections.CorePrefsImpl
import com.pitchedapps.frost.prefs.sections.FeedPrefs
import com.pitchedapps.frost.prefs.sections.FeedPrefsImpl
import com.pitchedapps.frost.prefs.sections.NotifPrefs
import com.pitchedapps.frost.prefs.sections.NotifPrefsImpl
import com.pitchedapps.frost.prefs.sections.ShowcasePrefs
import com.pitchedapps.frost.prefs.sections.ShowcasePrefsImpl
import com.pitchedapps.frost.prefs.sections.ThemePrefs
import com.pitchedapps.frost.prefs.sections.ThemePrefsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [Prefs] is no longer an actual pref, but we will expose the reset function as it is used
 * elsewhere
 */
interface PrefsBase {
  fun reset()
  fun deleteKeys(vararg keys: String)
}

interface Prefs :
  BehaviourPrefs, CorePrefs, FeedPrefs, NotifPrefs, ThemePrefs, ShowcasePrefs, PrefsBase

class PrefsImpl
@Inject
internal constructor(
  private val behaviourPrefs: BehaviourPrefs,
  private val corePrefs: CorePrefs,
  private val feedPrefs: FeedPrefs,
  private val notifPrefs: NotifPrefs,
  private val themePrefs: ThemePrefs,
  private val showcasePrefs: ShowcasePrefs
) :
  Prefs,
  BehaviourPrefs by behaviourPrefs,
  CorePrefs by corePrefs,
  FeedPrefs by feedPrefs,
  NotifPrefs by notifPrefs,
  ThemePrefs by themePrefs,
  ShowcasePrefs by showcasePrefs {

  override fun reset() {
    behaviourPrefs.reset()
    corePrefs.reset()
    feedPrefs.reset()
    notifPrefs.reset()
    themePrefs.reset()
    showcasePrefs.reset()
  }

  override fun deleteKeys(vararg keys: String) {
    behaviourPrefs.deleteKeys()
    corePrefs.deleteKeys()
    feedPrefs.deleteKeys()
    notifPrefs.deleteKeys()
    themePrefs.deleteKeys()
    showcasePrefs.deleteKeys()
  }
}

@Module
@InstallIn(SingletonComponent::class)
interface PrefModule {
  @Binds @Singleton fun behaviour(to: BehaviourPrefsImpl): BehaviourPrefs

  @Binds @Singleton fun core(to: CorePrefsImpl): CorePrefs

  @Binds @Singleton fun feed(to: FeedPrefsImpl): FeedPrefs

  @Binds @Singleton fun notif(to: NotifPrefsImpl): NotifPrefs

  @Binds @Singleton fun theme(to: ThemePrefsImpl): ThemePrefs

  @Binds @Singleton fun showcase(to: ShowcasePrefsImpl): ShowcasePrefs

  @Binds @Singleton fun prefs(to: PrefsImpl): Prefs
}

@Module
@InstallIn(SingletonComponent::class)
object PrefFactoryModule {
  @Provides
  @Singleton
  fun factory(@ApplicationContext context: Context): KPrefFactory = KPrefFactoryAndroid(context)
}
