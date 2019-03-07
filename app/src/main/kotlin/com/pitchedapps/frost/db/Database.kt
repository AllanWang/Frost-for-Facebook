package com.pitchedapps.frost.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext

interface FrostPrivateDao {
    fun cookieDao(): CookieDao
    fun notifDao(): NotificationDao
    fun cacheDao(): CacheDao
}

@Database(
    entities = [CookieEntity::class, NotificationEntity::class, CacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FrostPrivateDatabase : RoomDatabase(), FrostPrivateDao {
    companion object {
        const val DATABASE_NAME = "frost-priv-db"
    }
}

interface FrostPublicDao {
    fun tabDao(): FbTabDao
}

@Database(entities = [FbTabEntity::class], version = 1, exportSchema = true)
@TypeConverters(FbItemConverter::class)
abstract class FrostPublicDatabase : RoomDatabase(), FrostPublicDao {
    companion object {
        const val DATABASE_NAME = "frost-db"
    }
}

interface FrostDao : FrostPrivateDao, FrostPublicDao {
    fun close()
}

/**
 * Composition of all database interfaces
 */
class FrostDatabase(private val privateDb: FrostPrivateDatabase, private val publicDb: FrostPublicDatabase) :
    FrostDao,
    FrostPrivateDao by privateDb,
    FrostPublicDao by publicDb {

    override fun close() {
        privateDb.close()
        publicDb.close()
    }

    companion object {
        fun create(context: Context): FrostDatabase {
            val privateDb = Room.databaseBuilder(
                context, FrostPrivateDatabase::class.java,
                FrostPrivateDatabase.DATABASE_NAME
            ).build()
            val publicDb = Room.databaseBuilder(
                context, FrostPublicDatabase::class.java,
                FrostPublicDatabase.DATABASE_NAME
            ).build()
            return FrostDatabase(privateDb, publicDb)
        }

        fun module(context: Context) = module {
            single { create(context) }
            single { get<FrostDatabase>().cookieDao() }
            single { get<FrostDatabase>().tabDao() }
        }

        /**
         * Get from koin
         * For the most part, you can retrieve directly from other koin components
         */
        fun get(): FrostDatabase = StandAloneContext.getKoin().koinContext.get()
    }
}
