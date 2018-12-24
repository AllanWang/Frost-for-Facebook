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
package com.pitchedapps.frost.facebook.requests

import com.pitchedapps.frost.facebook.FB_URL_BASE

/**
 * Created by Allan Wang on 29/12/17.
 */
fun RequestAuth.markNotificationRead(notifId: Long): FrostRequest<Boolean> {

    val body = listOf(
        "click_type" to "notification_click",
        "id" to notifId,
        "target_id" to "null",
        "fb_dtsg" to fb_dtsg,
        "__user" to userId
    ).withEmptyData("m_sess", "__dyn", "__req", "__ajax__")

    return frostRequest(::executeForNoError) {
        url("${FB_URL_BASE}a/jewel_notifications_log.php")
        post(body.toForm())
    }
}

fun RequestAuth.markNotificationsRead(vararg notifId: Long) =
    notifId.toTypedArray().zip<Long, Boolean, Boolean>(
        { it.all { self -> self } },
        { markNotificationRead(it).invoke() })
