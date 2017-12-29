package com.pitchedapps.frost.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.BaseBundle
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

/**
 * Private helper data
 */
private enum class FrostRequestCommands {

    NOTIF_READ {

        override fun invoke(auth: RequestAuth, bundle: PersistableBundle) {
            val id = bundle.getLong(ARG_0, -1L)
            val success = auth.markNotificationRead(id).invoke()
            L.d("Marked notif $id as read: $success")
        }

        override fun propagate(bundle: BaseBundle) =
                FrostRunnable.prepareMarkNotificationRead(
                        bundle.getLong(ARG_0),
                        bundle.getCookie())

    };

    abstract fun invoke(auth: RequestAuth, bundle: PersistableBundle)

    abstract fun propagate(bundle: BaseBundle): BaseBundle.() -> Unit

    companion object {
        val values = values()
    }
}

private const val ARG_COMMAND = "frost_request_command"
private const val ARG_COOKIE = "frost_request_cookie"
private const val ARG_0 = "frost_request_arg_0"
private const val ARG_1 = "frost_request_arg_1"
private const val ARG_2 = "frost_request_arg_2"
private const val ARG_3 = "frost_request_arg_3"

private fun BaseBundle.getCookie() = getString(ARG_COOKIE)
private fun BaseBundle.putCookie(cookie: String) = putString(ARG_COOKIE, cookie)

/**
 * Singleton handler for running requests in [FrostRequestService]
 * Requests are typically completely decoupled from the UI,
 * and are optional enhancers.
 *
 * Nothing guarantees the completion time, or whether it even executes at all
 *
 * Design:
 * prepare function - creates a bundle binder
 * actor function   - calls the service with the given arguments
 *
 * Global:
 * propagator       - given a bundle with a command, extracts and executes the requests
 */
object FrostRunnable {

    fun prepareMarkNotificationRead(id: Long, cookie: String): BaseBundle.() -> Unit = {
        putLong(ARG_0, id)
        putCookie(cookie)
    }

    fun markNotificationRead(context: Context, id: Long, cookie: String): Boolean {
        if (id <= 0) {
            L.d("Invalid notification id $id for marking as read")
            return false
        }
        return schedule(context, FrostRequestCommands.NOTIF_READ,
                prepareMarkNotificationRead(id, cookie))
    }

    fun propagate(context: Context, bundle: BaseBundle?) {
        bundle ?: return
        val cmdIndex = bundle.getInt(ARG_COMMAND, -1)
        val command = FrostRequestCommands.values.getOrNull(cmdIndex) ?: return
        bundle.putInt(ARG_COMMAND, -1) // reset
        L.d("Propagating command ${command.name}")
        val builder = command.propagate(bundle)
        schedule(context, command, builder)
    }

    private fun schedule(context: Context,
                         command: FrostRequestCommands,
                         bundleBuilder: PersistableBundle.() -> Unit): Boolean {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val serviceComponent = ComponentName(context, FrostRequestService::class.java)
        val bundle = PersistableBundle()
        bundle.bundleBuilder()
        bundle.putInt(ARG_COMMAND, command.ordinal)

        if (bundle.getCookie().isNullOrBlank()) {
            L.e("Scheduled frost request with empty cookie)")
            return false
        }

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
        L.d("Scheduled ${command.name}")
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
            L.eThrow("Launched ${this::class.java.simpleName} without param data")
            return false
        }
        val now = System.currentTimeMillis()
        future = doAsync {
            val command = FrostRequestCommands.values[bundle.getInt(ARG_COMMAND)]
            bundle.getString(ARG_COOKIE).fbRequest {
                L.d("Requesting frost service for ${command.name}")
                command.invoke(this, bundle)
            }
            L.d("Finished frost service for ${command.name} in ${System.currentTimeMillis() - now} ms")
            jobFinished(params, false)
        }
        return true
    }
}