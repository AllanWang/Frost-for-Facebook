package com.pitchedapps.frost.extension

import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONObject

/**
 * Structure based off of
 *
 * https://github.com/mozilla-mobile/android-components/blob/main/components/feature/accounts/src/main/java/mozilla/components/feature/accounts/FxaWebChannelFeature.kt
 */
class FrostCoreExtension(
  private val customTabSessionId: String? = null,
  private val runtime: WebExtensionRuntime,
  private val store: BrowserStore,
  private val converter: ExtensionModelConverter,
) : LifecycleAwareFeature {

  private val extensionController =
    WebExtensionController(
      WEB_CHANNEL_EXTENSION_ID,
      WEB_CHANNEL_EXTENSION_URL,
      WEB_CHANNEL_MESSAGING_ID
    )

  private var scope: CoroutineScope? = null

  override fun start() {
    logger.atInfo().log("start")
    val messageHandler = FrostBackgroundMessageHandler()
    extensionController.registerBackgroundMessageHandler(
      messageHandler,
      WEB_CHANNEL_BACKGROUND_MESSAGING_ID,
    )

    extensionController.install(runtime)

    scope =
      store.flowScoped { flow ->
        flow
          .mapNotNull { state -> state.findCustomTabOrSelectedTab(customTabSessionId) }
          .ifChanged { it.engineState.engineSession }
          .collect {
            it.engineState.engineSession?.let { engineSession ->
              logger.atInfo().log("Register content message handler ${it.id}")
              registerContentMessageHandler(engineSession)
            }
          }
      }
  }

  override fun stop() {
    logger.atInfo().log("stop")
    scope?.cancel()
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
