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
package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import androidx.annotation.CallSuper
import ca.allanwang.kau.utils.ContextHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseJobService : JobService(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = ContextHelper.dispatcher + job

    protected val startTime = System.currentTimeMillis()

    /**
     * Note that if a job plans on running asynchronously, it should return true
     */
    @CallSuper
    override fun onStartJob(params: JobParameters?): Boolean {
        job = Job()
        return false
    }

    @CallSuper
    override fun onStopJob(params: JobParameters?): Boolean {
        job.cancel()
        return false
    }
}

/*
 * Collection of ids for job services.
 * These should all be unique
 */

const val NOTIFICATION_JOB_NOW = 6
const val NOTIFICATION_PERIODIC_JOB = 7
const val LOCAL_SERVICE_BASE = 110
const val REQUEST_SERVICE_BASE = 220
