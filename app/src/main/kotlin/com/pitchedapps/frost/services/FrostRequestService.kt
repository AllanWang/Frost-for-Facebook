package com.pitchedapps.frost.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.pitchedapps.frost.facebook.RequestAuth
import com.pitchedapps.frost.facebook.fbRequest
import com.pitchedapps.frost.facebook.markNotificationRead
import com.pitchedapps.frost.utils.L
import org.jetbrains.anko.doAsync
import java.util.concurrent.Future

/**
 * Created by Allan Wang on 28/12/17.
 */
private enum class FrostRequestCommands(
        val action: RequestAuth.(PersistableBundle) -> Unit) {

    NOTIF_READ({ markNotificationRead(it.getLong(ARG_0)) });

    companion object {
        val values = values()
    }
}

private const val ARG_COMMAND = "command"
private const val ARG_COOKIE = "cookie"
private const val ARG_0 = "arg_0"
private const val ARG_1 = "arg_1"
private const val ARG_2 = "arg_2"
private const val ARG_3 = "arg_3"

object FrostRunnable {

    fun markNotificationRead(context: Context, id: Long, cookie: String) =
            schedule(context, cookie, FrostRequestCommands.NOTIF_READ) {
                putLong(ARG_0, id)
            }

    private fun schedule(context: Context,
                         cookie: String,
                         command: FrostRequestCommands,
                         bundleBuilder: PersistableBundle.() -> Unit): Boolean {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val serviceComponent = ComponentName(context, FrostRequestService::class.java)
        val bundle = PersistableBundle()
        bundle.bundleBuilder()
        bundle.putInt(ARG_COMMAND, command.ordinal)
        bundle.putString(ARG_COOKIE, cookie)
        val builder = JobInfo.Builder(command.ordinal, serviceComponent)
                .setMinimumLatency(0L)
                .setExtras(bundle)
                .setOverrideDeadline(2000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        val result = scheduler.schedule(builder.build())
        if (result <= 0) {
            L.eThrow("FrostRequestService scheduler failed for ${command.name}")
            return false
        }
        return true
    }

}

class FrostRequestService : JobService() {

    var future: Future<Unit>? = null

    override fun onStopJob(params: JobParameters?): Boolean {
        future?.cancel(true)
        future = null
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val bundle = params?.extras
        if (bundle == null) {
            L.e("Launched ${this::class.java.simpleName} without param data")
            return false
        }
        future = doAsync {
            val command = FrostRequestCommands.values[bundle.getInt(ARG_COMMAND)]
            bundle.getString(ARG_COOKIE).fbRequest {
                L.d("Requesting frost service for ${command.name}")
                val action = command.action
                action(bundle)
            }
            L.d("Finished frost service for ${command.name}")
            jobFinished(params, false)
        }
        return true
    }
}