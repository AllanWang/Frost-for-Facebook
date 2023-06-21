/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.hilt

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject

/** Module containing Moshi injections. */
@Module
@InstallIn(SingletonComponent::class)
object MoshiModule {

  @Provides
  @Singleton
  fun moshi(): Moshi {
    return Moshi.Builder()
      // .add(ExtensionType.moshiFactory())
      .add(JSONObjectAdapter())
      .addLast(KotlinJsonAdapterFactory())
      .build()
  }

  private class JSONObjectAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): JSONObject? {
      // Handle map data only; ignore rest
      return (reader.readJsonValue() as? Map<*, *>)?.let { data ->
        try {
          JSONObject(data)
        } catch (e: JSONException) {
          null
        }
      }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONObject?) {
      value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
    }
  }
}
