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
import org.koin.core.context.KoinContextHandler
import org.koin.dsl.module

/**
 * [Prefs] is no longer an actual pref, but we will expose the reset function as it is used elsewhere
 */
interface PrefsBase {
    fun reset()
    fun deleteKeys(vararg keys: String)
}

interface Prefs :
    BehaviourPrefs,
    CorePrefs,
    FeedPrefs,
    NotifPrefs,
    ThemePrefs,
    ShowcasePrefs,
    PrefsBase {
    companion object {
        fun get(): Prefs = KoinContextHandler.get().get()

        fun module() = module {
            single<BehaviourPrefs> { BehaviourPrefsImpl(factory = get()) }
            single<CorePrefs> { CorePrefsImpl(factory = get()) }
            single<FeedPrefs> { FeedPrefsImpl(factory = get()) }
            single<NotifPrefs> { NotifPrefsImpl(factory = get()) }
            single<ThemePrefs> { ThemePrefsImpl(factory = get()) }
            single<ShowcasePrefs> { ShowcasePrefsImpl(factory = get()) }
            single<Prefs> {
                PrefsImpl(
                    behaviourPrefs = get(),
                    corePrefs = get(),
                    feedPrefs = get(),
                    notifPrefs = get(),
                    themePrefs = get(),
                    showcasePrefs = get()
                )
            }
            // Needed for migration
            single<OldPrefs> { OldPrefs(factory = get()) }
        }
    }
}

class PrefsImpl(
    private val behaviourPrefs: BehaviourPrefs,
    private val corePrefs: CorePrefs,
    private val feedPrefs: FeedPrefs,
    private val notifPrefs: NotifPrefs,
    private val themePrefs: ThemePrefs,
    private val showcasePrefs: ShowcasePrefs
) : Prefs,
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
