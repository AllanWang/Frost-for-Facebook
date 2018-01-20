package com.pitchedapps.frost.dbflow

import android.content.Context
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction

/**
 * Created by Allan Wang on 2017-05-30.
 */

object DbUtils {

    fun db(name: String) = FlowManager.getDatabase(name)
    fun dbName(name: String) = "$name.db"
    fun deleteDatabase(c: Context, name: String) = c.deleteDatabase(dbName(name))

}

inline fun <reified T : Any> List<T>.replace(dbName: String) {
    L.d { "Replacing $dbName.db" }
    DbUtils.db(dbName).reset()
    FastStoreModelTransaction.saveBuilder(FlowManager.getModelAdapter(T::class.java)).addAll(this).build()
}