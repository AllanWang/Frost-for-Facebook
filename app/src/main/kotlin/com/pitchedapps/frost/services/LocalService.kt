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

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.pitchedapps.frost.activities.ImageActivity
import com.pitchedapps.frost.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileFilter

class LocalService : BaseJobService() {

    enum class Flag {
        PURGE_IMAGE
    }

    companion object {
        private const val FLAG = "extra_local_flag"

        /**
         * Launches a local service with the provided flag
         */
        fun schedule(context: Context, flag: Flag): Boolean {
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val serviceComponent = ComponentName(context, LocalService::class.java)
            val bundle = PersistableBundle()
            bundle.putString(FLAG, flag.name)

            val builder = JobInfo.Builder(LOCAL_SERVICE_BASE + flag.ordinal, serviceComponent)
                .setMinimumLatency(0L)
                .setExtras(bundle)
                .setOverrideDeadline(2000L)

            val result = scheduler.schedule(builder.build())
            if (result <= 0) {
                L.eThrow("FrostRequestService scheduler failed for ${flag.name}")
                return false
            }
            L.d { "Scheduled ${flag.name}" }
            return true
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        super.onStartJob(params)
        val flagString = params?.extras?.getString(FLAG)
        val flag: Flag = try {
            Flag.valueOf(flagString!!)
        } catch (e: Exception) {
            L.e { "Local service with invalid flag $flagString" }
            return true
        }
        launch {
            when (flag) {
                Flag.PURGE_IMAGE -> purgeImages()
            }
        }
        return false
    }

    private suspend fun purgeImages() {
        withContext(Dispatchers.IO) {
            val purge = System.currentTimeMillis() - ImageActivity.PURGE_TIME
            ImageActivity.cacheDir(this@LocalService)
                .listFiles(FileFilter { it.isFile && it.lastModified() < purge })
                ?.forEach { it.delete() }
        }
    }
}
