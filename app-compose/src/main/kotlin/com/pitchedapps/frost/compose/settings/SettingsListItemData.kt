package com.pitchedapps.frost.compose.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
sealed interface SettingsListItemData {
  val title: String
  val enabled: Boolean
  val icon: ImageVector?
  val description: String?

  data class Item(
    override val title: String,
    override val enabled: Boolean = true,
    override val icon: ImageVector? = null,
    override val description: String? = null,
    val onClick: (() -> Unit)? = null,
  ) : SettingsListItemData

  data class Checkbox(
    override val title: String,
    override val enabled: Boolean = true,
    override val icon: ImageVector? = null,
    override val description: String? = null,
    val checked: Boolean,
    val onCheckChanged: (Boolean) -> Unit,
  ) : SettingsListItemData

  data class Switch(
    override val title: String,
    override val enabled: Boolean = true,
    override val icon: ImageVector? = null,
    override val description: String? = null,
    val checked: Boolean,
    val onCheckChanged: (Boolean) -> Unit,
  ) : SettingsListItemData
}
