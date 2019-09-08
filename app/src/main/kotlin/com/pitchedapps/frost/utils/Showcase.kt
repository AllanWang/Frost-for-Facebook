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

import ca.allanwang.kau.kpref.KPref

/**
 * Created by Allan Wang on 2017-07-03.
 *
 * Showcase prefs that offer one time helpers to guide new users
 */
object Showcase : KPref() {

    //check if this is the first time launching the web overlay; show snackbar if true
    val firstWebOverlay: Boolean by kprefSingle("first_web_overlay")

    val intro: Boolean by kprefSingle("intro_pages")

    override fun deleteKeys() = arrayOf("shown_release", "experimental_by_default")
}
