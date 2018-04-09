package com.pitchedapps.frost.utils

/**
 * Created by Allan Wang on 20/12/17.
 */
const val ACTIVITY_SETTINGS = 97
/*
 * Possible responses from the SettingsActivity
 * after the configurations have changed
 */
const val REQUEST_RESTART_APPLICATION = 1 shl 11
const val REQUEST_RESTART = 1 shl 12
const val REQUEST_REFRESH = 1 shl 13
const val REQUEST_TEXT_ZOOM = 1 shl 14
const val REQUEST_NAV = 1 shl 15
const val REQUEST_SEARCH = 1 shl 16

const val MAIN_TIMEOUT_DURATION = 30 * 60 * 1000 // 30 min

// Flavours
const val FLAVOUR_PRODUCTION = "production"
const val FLAVOUR_TEST = "releaseTest"
const val FLAVOUR_GITHUB = "github"
const val FLAVOUR_RELEASE = "release"
const val FLAVOUR_UNNAMED = "unnamed"