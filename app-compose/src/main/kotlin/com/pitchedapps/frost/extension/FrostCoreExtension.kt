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

import androidx.lifecycle.LifecycleOwner
import com.google.common.flogger.FluentLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.lib.state.ext.flow
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONObject

/**
 * Frost's built in extension.
 *
 * Structure based off of
 *
 * https://github.com/mozilla-mobile/android-components/blob/main/components/feature/accounts/src/main/java/mozilla/components/feature/accounts/FxaWebChannelFeature.kt
 */
@Singleton
class FrostCoreExtension
@Inject
internal constructor(
  private val engine: Engine,
  private val store: BrowserStore,
  private val converter: ExtensionModelConverter,
) {

  private val extensionController =
    WebExtensionController(
      WEB_CHANNEL_EXTENSION_ID,
      WEB_CHANNEL_EXTENSION_URL,
      WEB_CHANNEL_MESSAGING_ID,
    )

  fun install() {
    logger.atInfo().log("extension background start")
    val messageHandler = FrostBackgroundMessageHandler()
    extensionController.registerBackgroundMessageHandler(
      messageHandler,
      WEB_CHANNEL_BACKGROUND_MESSAGING_ID,
    )

    extensionController.install(
      engine,
      onSuccess = {
        logger.atInfo().log("extension install success")
        extensionController.sendBackgroundMessage(
          JSONObject().apply { put("test", 0) },
          WEB_CHANNEL_BACKGROUND_MESSAGING_ID
        )
      },
      onError = { t -> logger.atWarning().withCause(t).log("extension install failure") },
    )
  }

  suspend fun installContent(owner: LifecycleOwner? = null, customTabSessionId: String? = null) {
    logger.atInfo().log("extension content start")

    store
      .flow(owner)
      .mapNotNull { state -> state.findCustomTabOrSelectedTab(customTabSessionId) }
      .ifChanged { it.engineState.engineSession }
      .collect {
        it.engineState.engineSession?.let { engineSession ->
          logger.atInfo().log("Register content message handler ${it.id}")
          registerContentMessageHandler(engineSession)
        }
      }
  }

  private fun registerContentMessageHandler(engineSession: EngineSession) {
    val messageHandler = FrostMessageHandler(converter)
    extensionController.registerContentMessageHandler(engineSession, messageHandler)
  }

  private class FrostBackgroundMessageHandler : MessageHandler {

    override fun onMessage(message: Any, source: EngineSession?): Any? {
      logger.atInfo().log("onMessage: %s", message)
      return null
    }

    override fun onPortConnected(port: Port) {
      logger.atInfo().log("background onPortConnected: %s", port.name())
    }
  }

  private class FrostMessageHandler(private val converter: ExtensionModelConverter) :
    MessageHandler {
    override fun onMessage(message: Any, source: EngineSession?): Any? {
      if (message is String) {
        logger.atInfo().log("onMessage: %s", message)
        return null
      }
      val model = converter.fromJSONObject(message as? JSONObject)
      if (model == null) {
        logger.atWarning().log("onMessage - unexpected format: %s", message)
        return null
      }
      logger.atFine().log("onMessage: %s", model)
      when (model) {
        is UrlClick -> {
          logger.atInfo().log("UrlClick ${model.url}")

          return true
        }
        else -> {
          logger.atWarning().log("onMessage - unhandled: %s", model)
        }
      }

      return null
    }

    override fun onPortConnected(port: Port) {
      logger.atInfo().log("content onPortConnected")
      super.onPortConnected(port)
    }

    override fun onPortMessage(message: Any, port: Port) {
      if (message is String) {
        logger.atInfo().log("onPortMessage: %s", message)
        return
      }
      val model = converter.fromJSONObject(message as? JSONObject)
      if (model == null) {
        logger.atWarning().log("onPortMessage - unexpected format: %s", message)
        return
      }
      logger.atFine().log("onPortMessage: %s", model)
      // TODO
    }

    private fun Port.postMessage(message: ExtensionModel) {
      val json = converter.toJSONObject(message)
      if (json == null) {
        logger.atSevere().log("postMessage - unexpected format: %s", message)
        return
      }
      postMessage(json)
    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    const val WEB_CHANNEL_EXTENSION_ID = "frost_gecko_core@pitchedapps"
    const val WEB_CHANNEL_MESSAGING_ID = "frostChannel"
    const val WEB_CHANNEL_BACKGROUND_MESSAGING_ID = "frostBackgroundChannel"
    const val WEB_CHANNEL_EXTENSION_URL = "resource://android/assets/frostcore/"
  }
}
