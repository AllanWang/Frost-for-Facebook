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

object BuildUtils {

    data class Data(val versionName: String, val tail: String)

    // Builds
    private const val BUILD_PRODUCTION = "production"
    private const val BUILD_TEST = "releaseTest"
    private const val BUILD_GITHUB = "github"
    private const val BUILD_RELEASE = "release"
    private const val BUILD_UNNAMED = "unnamed"

    fun match(version: String): Data? {
        val regex = Regex("([0-9]+\\.[0-9]+\\.[0-9]+)-?(.*?)")
        val result = regex.matchEntire(version)?.groupValues ?: return null
        return Data("v${result[1]}", result[2])
    }

    fun getAllStages(): Set<String> =
        setOf(BUILD_PRODUCTION, BUILD_TEST, BUILD_GITHUB, BUILD_RELEASE, BUILD_UNNAMED)

    fun getStage(build: String): String = build.takeIf { it in getAllStages() } ?: BUILD_UNNAMED
}
