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
package com.pitchedapps.frost.facebook

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons.Default as MaterialIcons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.pitchedapps.frost.R
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.main.MainTabItem
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.GlobeAmericas

/**
 * Fb page info.
 *
 * All pages here are independent, in that they can be loaded directly. [key] must be final, as it
 * is used to store tab info.
 */
enum class FbItem(
  val key: String,
  @StringRes val titleId: Int,
  val icon: ImageVector,
  val relativeUrl: String,
) {
  ActivityLog(
    key = "activity_log",
    titleId = R.string.activity_log,
    icon = MaterialIcons.List,
    relativeUrl = "me/allactivity",
  ),
  Birthdays(
    key = "birthdays",
    titleId = R.string.birthdays,
    icon = MaterialIcons.Cake,
    relativeUrl = "events/birthdays",
  ),
  Events("events", R.string.events, MaterialIcons.EventNote, "events/upcoming"),
  Feed("feed", R.string.feed, MaterialIcons.Newspaper, ""),
  FeedMostRecent(
    "feed_most_recent",
    R.string.most_recent,
    MaterialIcons.History,
    "home.php?sk=h_chr",
  ),
  FeedTopStories("feed_top_stories", R.string.top_stories, MaterialIcons.Star, "home.php?sk=h_nor"),
  Friends("friends", R.string.friends, MaterialIcons.PersonAddAlt1, "friends/center/requests"),
  Groups("groups", R.string.groups, MaterialIcons.Group, "groups"),
  Marketplace("marketplace", R.string.marketplace, MaterialIcons.Store, "marketplace"),
  Menu("menu", R.string.menu, MaterialIcons.Menu, "bookmarks"),
  Messages("messages", R.string.messages, MaterialIcons.ChatBubble, "messages"),
  Notes("notes", R.string.notes, MaterialIcons.Note, "notes"),
  Notifications(
    "notifications",
    R.string.notifications,
    FontAwesomeIcons.Solid.GlobeAmericas,
    "notifications",
  ),
  OnThisDay("on_this_day", R.string.on_this_day, MaterialIcons.Today, "onthisday"),
  Pages("pages", R.string.pages, MaterialIcons.Flag, "pages"),
  Photos("photos", R.string.photos, MaterialIcons.Photo, "me/photos"),
  Profile("profile", R.string.profile, MaterialIcons.AccountCircle, "me"),
  Saved("saved", R.string.saved, MaterialIcons.Bookmark, "saved"),
  Settings("settings", R.string.settings, MaterialIcons.Settings, "settings"),
  ;

  val url = "$FB_URL_BASE$relativeUrl"

  val isFeed: Boolean
    get() =
      when (this) {
        Feed,
        FeedMostRecent,
        FeedTopStories -> true
        else -> false
      }

  companion object {
    private val values = values().associateBy { it.key }

    fun fromKey(key: String) = values[key]

    fun defaults() = listOf(Feed, Messages, Notifications, Menu)
  }
}

/** Converts [FbItem] to [MainTabItem]. */
fun FbItem.tab(context: Context, id: WebTargetId): MainTabItem =
  MainTabItem(
    id = id,
    title = context.getString(titleId),
    icon = icon,
    url = url,
  )


/// ** Note that this url only works if a query (?q=) is provided */
// _SEARCH("search", R.string.kau_search, GoogleMaterial.Icon.gmd_search, "search/top"),
//
/// ** Non mbasic search cannot be parsed. */
// _SEARCH_PARSE(
// R.string.kau_search,
// GoogleMaterial.Icon.gmd_search,
// "search/top",
// prefix = FB_URL_MBASIC_BASE,
// ),
