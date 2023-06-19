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
package com.pitchedapps.frost.components.usecases

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases

/** Module for Mozilla use cases. */
@Module
@InstallIn(SingletonComponent::class)
class UseCasesModule {

  @Provides @Singleton fun sessionUseCases(store: BrowserStore) = SessionUseCases(store)

  @Provides @Singleton fun tabsUseCases(store: BrowserStore) = TabsUseCases(store)

  @Provides @Singleton fun contextMenuUseCases(store: BrowserStore) = ContextMenuUseCases(store)

  @Provides @Singleton fun downloadsUseCases(store: BrowserStore) = DownloadsUseCases(store)

  @Provides
  @Singleton
  fun appLinksUseCases(@ApplicationContext context: Context) = AppLinksUseCases(context)
}
