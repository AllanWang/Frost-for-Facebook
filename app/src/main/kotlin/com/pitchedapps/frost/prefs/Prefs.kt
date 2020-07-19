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
import com.pitchedapps.frost.prefs.sections.ThemePrefs
import com.pitchedapps.frost.prefs.sections.ThemePrefsImpl
import org.koin.core.context.KoinContextHandler
import org.koin.dsl.module

interface Prefs : BehaviourPrefs, CorePrefs, FeedPrefs, NotifPrefs, ThemePrefs {
    companion object {
        fun get(): Prefs = KoinContextHandler.get().get()

        fun module() = module {
            single<BehaviourPrefs> { BehaviourPrefsImpl(factory = get()) }
            single<CorePrefs> { CorePrefsImpl(factory = get()) }
            single<FeedPrefs> { FeedPrefsImpl(factory = get()) }
            single<NotifPrefs> { NotifPrefsImpl(factory = get()) }
            single<ThemePrefs> { ThemePrefsImpl(factory = get()) }
            single<Prefs> {
                PrefsImpl(
                    behaviourPrefs = get(),
                    corePrefs = get(),
                    feedPrefs = get(),
                    notifPrefs = get(),
                    themePrefs = get()
                )
            }
            // Needed for migration
            single<OldPrefs> { OldPrefs(factory = get()) }
        }
    }
}

class PrefsImpl(
    behaviourPrefs: BehaviourPrefs,
    corePrefs: CorePrefs,
    feedPrefs: FeedPrefs,
    notifPrefs: NotifPrefs,
    themePrefs: ThemePrefs
) : Prefs,
    BehaviourPrefs by behaviourPrefs,
    CorePrefs by corePrefs,
    FeedPrefs by feedPrefs,
    NotifPrefs by notifPrefs,
    ThemePrefs by themePrefs
