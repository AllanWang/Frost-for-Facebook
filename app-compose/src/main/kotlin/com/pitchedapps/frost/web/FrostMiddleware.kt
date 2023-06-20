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
package com.pitchedapps.frost.web

import com.google.common.flogger.FluentLogger
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext

class FrostLoggerMiddleware(private val tag: String) : Middleware<FrostWebState, FrostAction> {
  override fun invoke(
    context: MiddlewareContext<FrostWebState, FrostAction>,
    next: (FrostAction) -> Unit,
    action: FrostAction
  ) {
    logger.atInfo().log("FrostWebAction-%s: %s - %s", tag, action::class.simpleName, action)
    next(action)
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
