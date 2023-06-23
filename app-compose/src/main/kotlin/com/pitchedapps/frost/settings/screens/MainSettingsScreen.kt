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

import androidx.compose.material.icons.Icons.Outlined as MaterialIcons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pitchedapps.frost.R
import com.pitchedapps.frost.compose.settings.SettingsListDsl

@Composable
fun MainSettingsScreen(modifier: Modifier = Modifier) {
  SettingsListDsl(modifier = modifier) {
    item(
      icon = MaterialIcons.Palette,
      title = stringResource(id = R.string.appearance),
      description = stringResource(id = R.string.appearance_desc),
    )
    item(
      icon = MaterialIcons.TrendingUp,
      title = stringResource(id = R.string.behaviour),
      description = stringResource(id = R.string.behaviour_desc),
    )
    item(
      icon = MaterialIcons.Newspaper,
      title = stringResource(id = R.string.newsfeed),
      description = stringResource(id = R.string.newsfeed_desc),
    )
    item(
      icon = MaterialIcons.Notifications,
      title = stringResource(id = R.string.notifications),
      description = stringResource(id = R.string.notifications_desc),
    )
    item(
      icon = MaterialIcons.Lock,
      title = stringResource(id = R.string.security),
      description = stringResource(id = R.string.security_desc),
    )
    item(
      icon = MaterialIcons.Info,
      title = stringResource(id = R.string.about_frost),
      description = stringResource(id = R.string.about_frost_desc),
    )
    item(
      icon = MaterialIcons.Translate,
      title = stringResource(id = R.string.help_translate),
      description = stringResource(id = R.string.help_translate_desc),
    )
    item(
      icon = MaterialIcons.Replay,
      title = stringResource(id = R.string.replay_intro),
    )
    //    item(
    //      icon = MaterialIcons.Science,
    //      title = stringResource(id = R.string.experimental),
    //      description = stringResource(id = R.string.experimental_desc),
    //    )
  }
}

@Preview
@Composable
fun MainSettingsScreenPreview() {
  MaterialTheme { MainSettingsScreen() }
}
