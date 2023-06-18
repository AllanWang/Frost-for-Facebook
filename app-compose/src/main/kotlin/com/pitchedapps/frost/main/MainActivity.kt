package com.pitchedapps.frost.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    logger.atInfo().log("onCreate main activity activity")
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent { MaterialTheme { MainScreen(modifier = Modifier.systemBarsPadding()) } }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }

}
