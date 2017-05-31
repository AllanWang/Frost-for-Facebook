package com.pitchedapps.frost.utils

import android.content.Context
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.processInTransactionAsync
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Created by Allan Wang on 2017-05-30.
 */
object DbUtils {

    fun db(name: String) = FlowManager.getDatabase(name)
    fun dbName(name: String) = "$name.db"

    inline fun <reified T : BaseModel> replace(
            context: Context, dbName: String, type: Class<T>, data: List<T>,
            crossinline callback: ((successful: Boolean) -> Unit)) {
        db(dbName).reset(context)
        data.processInTransactionAsync({
            t, databaseWrapper ->
            t.save(databaseWrapper)
        },
                Transaction.Success {
                    callback.invoke(true)
                },
                Transaction.Error { _, error ->
                    L.e(error.message ?: "DbReplace error")
                    callback.invoke(false)
                })
    }
}
