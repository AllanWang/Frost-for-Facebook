/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.components

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.permission.SitePermissionsStorage
import org.mozilla.geckoview.GeckoRuntime

@Singleton
class Core
@Inject
internal constructor(
  private val runtimeProvider: Provider<GeckoRuntime>,
  private val engineProvider: Provider<Engine>,
  private val storeProvider: Provider<BrowserStore>,
  private val sessionStorageProvider: Provider<SessionStorage>,
  private val sitePermissionsStorageProvider: Provider<SitePermissionsStorage>,
) {
  val runtime: GeckoRuntime
    get() = runtimeProvider.get()

  val engine: Engine
    get() = engineProvider.get()

  val store: BrowserStore
    get() = storeProvider.get()

  val sessionStorage: SessionStorage
    get() = sessionStorageProvider.get()

  val sitePermissionsStorage: SitePermissionsStorage
    get() = sitePermissionsStorageProvider.get()
}
