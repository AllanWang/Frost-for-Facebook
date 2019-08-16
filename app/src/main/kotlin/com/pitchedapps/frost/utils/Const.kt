/*
 * Copyright 2018 Allan Wang
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
package com.pitchedapps.frost.utils

/**
 * Created by Allan Wang on 20/12/17.
 */
const val ACTIVITY_SETTINGS = 97
/*
 * Possible responses from the SettingsActivity
 * after the configurations have changed.
 * Note that the first few bits are restricted to position related requests
 */
const val REQUEST_RESTART_APPLICATION = 1 shl 5
const val REQUEST_RESTART = 1 shl 6
const val REQUEST_REFRESH = 1 shl 7
const val REQUEST_TEXT_ZOOM = 1 shl 8
const val REQUEST_NAV = 1 shl 9
const val REQUEST_SEARCH = 1 shl 10
const val REQUEST_FAB = 1 shl 11
const val REQUEST_NOTIFICATION = 1 shl 12

const val MAIN_TIMEOUT_DURATION = 30 * 60 * 1000 // 30 min
