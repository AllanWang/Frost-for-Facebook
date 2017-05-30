package com.pitchedapps.frost.utils

import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Allan Wang on 2017-05-29.
 */
@JvmOverloads fun realm(name: String = RealmFiles.main, transaction: Realm.Transaction) {
    val realm = Realm.getInstance(RealmConfiguration.Builder().name(name).build())
    realm.executeTransaction(transaction)
    realm.close()
}

object RealmFiles {
    val main = "frost.realm"
    val TABS = "tabs.realm"
}