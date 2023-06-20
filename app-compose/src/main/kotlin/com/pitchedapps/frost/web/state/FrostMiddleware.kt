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
package com.pitchedapps.frost.web.state

import com.google.common.flogger.FluentLogger
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext

typealias FrostWebMiddleware = Middleware<FrostWebState, FrostWebAction>

class FrostLoggerMiddleware : FrostWebMiddleware {
  override fun invoke(
    context: MiddlewareContext<FrostWebState, FrostWebAction>,
    next: (FrostWebAction) -> Unit,
    action: FrostWebAction
  ) {
    logger.atInfo().log("FrostWebAction: %s - %s", action::class.simpleName, action)
    next(action)
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}

class FrostCookieMiddleware : FrostWebMiddleware {
  override fun invoke(
    context: MiddlewareContext<FrostWebState, FrostWebAction>,
    next: (FrostWebAction) -> Unit,
    action: FrostWebAction
  ) {
    when (action) {
      else -> next(action)
    }
  }
}
