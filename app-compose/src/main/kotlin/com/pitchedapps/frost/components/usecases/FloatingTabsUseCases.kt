package com.pitchedapps.frost.components.usecases

import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore

@Singleton
class FloatingTabsUseCases @Inject internal constructor(private val store: BrowserStore) {

  fun createFloatingTab(url: String) {
    if (store.state.findTab(TAB_ID) == null) {
      val tab = createTab(url = url, id = TAB_ID)
      store.dispatch(TabListAction.AddTabAction(tab = tab, select = false))
    }
    store.dispatch(EngineAction.LoadUrlAction(tabId = TAB_ID, url = url))
  }

  fun removeFloatingTab() {
    store.dispatch(TabListAction.RemoveTabAction(tabId = TAB_ID))
  }

  companion object {
    const val TAB_ID = "floating_tab_id"
  }
}
