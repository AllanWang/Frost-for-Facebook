/*
 * Copyright 2019 Allan Wang
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
package com.pitchedapps.frost.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pitchedapps.frost.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface FrostPrivateDao {
  fun cookieDao(): CookieDao
  fun notifDao(): NotificationDao
  fun cacheDao(): CacheDao
}

@Database(
  entities = [CookieEntity::class, NotificationEntity::class, CacheEntity::class],
  version = 2,
  exportSchema = true
)
abstract class FrostPrivateDatabase : RoomDatabase(), FrostPrivateDao {
  companion object {
    const val DATABASE_NAME = "frost-priv-db"
  }
}

interface FrostPublicDao {
  fun genericDao(): GenericDao
}

@Database(entities = [GenericEntity::class], version = 1, exportSchema = true)
abstract class FrostPublicDatabase : RoomDatabase(), FrostPublicDao {
  companion object {
    const val DATABASE_NAME = "frost-db"
  }
}

interface FrostDao : FrostPrivateDao, FrostPublicDao {
  fun close()
}

/** Composition of all database interfaces */
class FrostDatabase(
  private val privateDb: FrostPrivateDatabase,
  private val publicDb: FrostPublicDatabase
) : FrostDao, FrostPrivateDao by privateDb, FrostPublicDao by publicDb {

  override fun close() {
    privateDb.close()
    publicDb.close()
  }

  companion object {

    private fun <T : RoomDatabase> RoomDatabase.Builder<T>.frostBuild() =
      if (BuildConfig.DEBUG) {
        fallbackToDestructiveMigration().build()
      } else {
        build()
      }

    fun create(context: Context): FrostDatabase {
      val privateDb =
        Room.databaseBuilder(
            context,
            FrostPrivateDatabase::class.java,
            FrostPrivateDatabase.DATABASE_NAME
          )
          .addMigrations(COOKIES_MIGRATION_1_2)
          .frostBuild()
      val publicDb =
        Room.databaseBuilder(
            context,
            FrostPublicDatabase::class.java,
            FrostPublicDatabase.DATABASE_NAME
          )
          .frostBuild()
      return FrostDatabase(privateDb, publicDb)
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun frostDatabase(@ApplicationContext context: Context): FrostDatabase =
    FrostDatabase.create(context)

  @Provides
  @Singleton
  fun cookieDao(frostDatabase: FrostDatabase): CookieDao = frostDatabase.cookieDao()

  @Provides
  @Singleton
  fun cacheDao(frostDatabase: FrostDatabase): CacheDao = frostDatabase.cacheDao()

  @Provides
  @Singleton
  fun notifDao(frostDatabase: FrostDatabase): NotificationDao = frostDatabase.notifDao()

  @Provides
  @Singleton
  fun genericDao(frostDatabase: FrostDatabase): GenericDao = frostDatabase.genericDao()
}
