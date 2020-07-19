/*
 * Copyright 2020 Allan Wang
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
package com.pitchedapps.frost.prefs.sections

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.prefs.OldPrefs
import com.pitchedapps.frost.prefs.PrefsBase
import org.koin.core.KoinComponent
import org.koin.core.inject

interface CorePrefs : PrefsBase {
    var lastLaunch: Long

    var userId: Long

    var prevId: Long

    val frostId: String

    var versionCode: Int

    var prevVersionCode: Int

    var installDate: Long

    var identifier: Int

    /**
     * Despite the naming, this toggle currently only enables debug logging.
     * Verbose is never logged in release builds.
     */
    var verboseLogging: Boolean

    /**
     * True to enable analytic reports (BugSnag)
     */
    var analytics: Boolean

    var enablePip: Boolean

    var exitConfirmation: Boolean

    var animate: Boolean

    var messageScrollToBottom: Boolean
}

class CorePrefsImpl(
    factory: KPrefFactory
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.core", factory),
    CorePrefs, KoinComponent {

    private val oldPrefs: OldPrefs by inject()

    override var lastLaunch: Long by kpref("last_launch", oldPrefs.lastLaunch /* -1L */)

    override var userId: Long by kpref("user_id", oldPrefs.userId /* -1L */)

    override var prevId: Long by kpref("prev_id", oldPrefs.prevId /* -1L */)

    override val frostId: String
        get() = "$installDate-$identifier"

    override var versionCode: Int by kpref("version_code", oldPrefs.versionCode /* -1 */)

    override var prevVersionCode: Int by kpref(
        "prev_version_code",
        oldPrefs.prevVersionCode /* -1 */
    )

    override var installDate: Long by kpref("install_date", oldPrefs.installDate /* -1L */)

    override var identifier: Int by kpref("identifier", oldPrefs.identifier /* -1 */)

    override var verboseLogging: Boolean by kpref(
        "verbose_logging",
        oldPrefs.verboseLogging /* false */
    )

    override var analytics: Boolean by kpref("analytics", oldPrefs.analytics /* false */) {
//        if (!BuildConfig.DEBUG) {
//            if (it) {
//                Bugsnag.setAutoCaptureSessions(true)
//                Bugsnag.enableExceptionHandler()
//            } else {
//                Bugsnag.setAutoCaptureSessions(false)
//                Bugsnag.disableExceptionHandler()
//            }
//        }
    }

    override var enablePip: Boolean by kpref("enable_pip", oldPrefs.enablePip /* true */)

    override var exitConfirmation: Boolean by kpref(
        "exit_confirmation",
        oldPrefs.exitConfirmation /* true */
    )

    override var animate: Boolean by kpref("fancy_animations", oldPrefs.animate /* true */)

    override var messageScrollToBottom: Boolean by kpref(
        "message_scroll_to_bottom",
        oldPrefs.messageScrollToBottom /* false */
    )
}
