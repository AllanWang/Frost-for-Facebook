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
package com.pitchedapps.frost.settings.screens

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons.Outlined as MaterialIcons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pitchedapps.frost.R
import com.pitchedapps.frost.compose.FrostPreview
import com.pitchedapps.frost.compose.settings.SettingsList
import com.pitchedapps.frost.compose.settings.SettingsListItemData

@Composable
fun MainSettingsScreen(modifier: Modifier = Modifier) {

  val data = listOf(
    SettingsListItemData.Item(
      icon = MaterialIcons.Palette,
      title = stringResource(id = R.string.appearance),
      description = stringResource(id = R.string.appearance_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.TrendingUp,
      title = stringResource(id = R.string.behaviour),
      description = stringResource(id = R.string.behaviour_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Newspaper,
      title = stringResource(id = R.string.newsfeed),
      description = stringResource(id = R.string.newsfeed_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Notifications,
      title = stringResource(id = R.string.notifications),
      description = stringResource(id = R.string.notifications_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Lock,
      title = stringResource(id = R.string.security),
      description = stringResource(id = R.string.security_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Info,
      title = stringResource(id = R.string.about_frost),
      description = stringResource(id = R.string.about_frost_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Translate,
      title = stringResource(id = R.string.help_translate),
      description = stringResource(id = R.string.help_translate_desc),
    ),
    SettingsListItemData.Item(
      icon = MaterialIcons.Replay,
      title = stringResource(id = R.string.replay_intro),
    ),
  )

  SettingsList(modifier = modifier, data = data)
}

@Preview
@Composable
fun MainSettingsScreenPreview() {
  FrostPreview { MainSettingsScreen(modifier = Modifier.systemBarsPadding()) }
}
