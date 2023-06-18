package com.pitchedapps.frost.components

import com.pitchedapps.frost.components.usecases.FloatingTabsUseCases
import com.pitchedapps.frost.components.usecases.HomeTabsUseCases
import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * Collection of use cases.
 *
 * Note that included use cases are not lazily loaded.
 */
@Singleton
class UseCases
@Inject
internal constructor(
  val session: SessionUseCases,
  val tabs: TabsUseCases,
  val homeTabs: HomeTabsUseCases,
  val floatingTabs: FloatingTabsUseCases,
)
