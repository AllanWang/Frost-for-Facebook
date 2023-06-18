package com.pitchedapps.frost.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
inline fun <reified T : Activity> Context.launchActivity(
  clearStack: Boolean = false,
  bundleBuilder: Bundle.() -> Unit = {},
  intentBuilder: Intent.() -> Unit = {}
) {
  val intent = Intent(this, T::class.java)
  if (clearStack) {
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  intent.intentBuilder()
  val bundle = Bundle()
  bundle.bundleBuilder()
  startActivity(intent, bundle.takeIf { !it.isEmpty })
  if (clearStack && this is Activity) {
    finish()
  }
}
