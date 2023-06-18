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
package com.pitchedapps.frost.extension

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.AddonsProvider
import mozilla.components.feature.addons.amo.AddonCollectionProvider
import mozilla.components.feature.addons.update.AddonUpdater
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.support.base.android.NotificationsDelegate

@Module
@InstallIn(SingletonComponent::class)
object FrostAddOnsModule {

  // https://services.addons.mozilla.org/api/v4/accounts/account/17569911/collections/Frost-for-Facebook/addons/?page_size=10&lang=en-US
  @Provides
  @Singleton
  fun addonsProvider(@ApplicationContext context: Context, client: Client): AddonsProvider {
    return AddonCollectionProvider(
      context = context,
      client = client,
      collectionUser = "17569911",
      collectionName = "Frost-for-Facebook",
    )
  }

  @Provides
  @Singleton
  fun addonUpdater(
    @ApplicationContext context: Context,
    notificationsDelegate: NotificationsDelegate
  ): AddonUpdater {
    return DefaultAddonUpdater(
      applicationContext = context,
      notificationsDelegate = notificationsDelegate
    )
  }

  @Provides
  @Singleton
  fun addonManager(
    store: BrowserStore,
    engine: Engine,
    addonsProvider: AddonsProvider,
    addonUpdater: AddonUpdater
  ): AddonManager {
    return AddonManager(
      store = store,
      runtime = engine,
      addonsProvider = addonsProvider,
      addonUpdater = addonUpdater
    )
  }
}
