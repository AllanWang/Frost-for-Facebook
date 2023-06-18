package com.pitchedapps.frost.hilt

import com.pitchedapps.frost.extension.ExtensionType
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

@Module
@InstallIn(SingletonComponent::class)
object MoshiModule {

  @Provides
  @Singleton
  fun moshi(): Moshi {
    return Moshi.Builder()
      .add(ExtensionType.moshiFactory())
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
