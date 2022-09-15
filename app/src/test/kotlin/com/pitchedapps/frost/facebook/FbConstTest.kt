/*
 * Copyright 2021 Allan Wang
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

import kotlin.test.Test
import kotlin.test.assertFalse

class FbConstTest {

  private val constants =
    listOf(
      FACEBOOK_COM,
      MESSENGER_COM,
      FBCDN_NET,
      WWW_FACEBOOK_COM,
      WWW_MESSENGER_COM,
      HTTPS_FACEBOOK_COM,
      HTTPS_MESSENGER_COM,
      FACEBOOK_BASE_COM,
      FB_URL_BASE,
      FACEBOOK_MBASIC_COM,
      FB_URL_MBASIC_BASE,
      FB_LOGIN_URL,
      FB_HOME_URL,
      MESSENGER_THREAD_PREFIX
    )

  /** Make sure we don't have accidental double forward slashes after appending */
  @Test
  fun doubleForwardSlashTest() {
    constants.forEach {
      assertFalse(it.replace("https://", "").contains("//"), "Accidental forward slash for $it")
    }
  }
}
