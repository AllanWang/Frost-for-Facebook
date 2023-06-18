package com.pitchedapps.frost.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel
@Inject
internal constructor(

) : ViewModel() {

  var tabIndex: Int by mutableStateOf(0)
}
