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
