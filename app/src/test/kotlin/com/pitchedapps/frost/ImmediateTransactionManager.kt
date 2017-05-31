package com.pitchedapps.frost

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
/**
 * Created by Allan Wang on 2017-05-30.
 */
class ImmediateTransactionManager(databaseDefinition: DatabaseDefinition)
    : BaseTransactionManager(ImmediateTransactionQueue(), databaseDefinition)


class ImmediateTransactionQueue : ITransactionQueue {

    override fun add(transaction: Transaction?) {
        if (transaction != null) {
            transaction.newBuilder()
                    .runCallbacksOnSameThread(true)
                    .build()
                    .executeSync()
        }
    }

    override fun cancel(transaction: Transaction?) {

    }

    override fun startIfNotAlive() {
    }

    override fun cancel(name: String?) {
    }

    override fun quit() {
    }

}