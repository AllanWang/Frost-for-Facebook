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
