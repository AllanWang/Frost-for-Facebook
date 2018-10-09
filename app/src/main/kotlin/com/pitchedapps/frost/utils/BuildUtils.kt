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
        val regex = Regex("([0-9]+\\.[0-9]+\\.[0-9]+)-?([0-9]*-?[0-9a-zA-Z]*)")
        val result = regex.matchEntire(version)?.groupValues ?: return null
        return Data(result[1], result[2])
    }

    fun getAllStages(): Array<String> =
            arrayOf(BUILD_PRODUCTION, BUILD_TEST, BUILD_GITHUB, BUILD_RELEASE, BUILD_UNNAMED)

    fun getStage(build: String): String = build.takeIf { it in getAllStages() } ?: BUILD_UNNAMED

}