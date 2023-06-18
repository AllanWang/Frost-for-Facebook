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
package com.pitchedapps.frost.extension

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

object ExtensionType {
  const val TEST = "type-test"
  const val URL_CLICK = "url-click"

  fun moshiFactory(): JsonAdapter.Factory {
    return PolymorphicJsonAdapterFactory.of(ExtensionModel::class.java, "type")
      .withSubtype(TestModel::class.java, TEST)
      .withSubtype(UrlClick::class.java, URL_CLICK)
  }
}

/**
 * kotlinx.serialization seems to support polymorphism:
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 *
 * But it won't work for us since we receive data from JS without metadata. As a result, we will map
 * the decoding ourself.
 */
sealed interface ExtensionModel {
  companion object
}

@JsonClass(generateAdapter = true)
data class TestModel(val message: String = "Test Model Message") : ExtensionModel

@JsonClass(generateAdapter = true) data class UrlClick(val url: String) : ExtensionModel

@OptIn(ExperimentalStdlibApi::class)
@Singleton
class ExtensionModelConverter @Inject internal constructor(moshi: Moshi) {
  private val jsonObjectAdapter = moshi.adapter<JSONObject>()
  private val modelAdapter = moshi.adapter<ExtensionModel>()

  fun toJSONObject(value: ExtensionModel): JSONObject? {
    val jsonValue = modelAdapter.toJsonValue(value) ?: return null
    return jsonObjectAdapter.fromJsonValue(jsonValue)
  }

  fun fromJSONObject(value: JSONObject?): ExtensionModel? {
    value ?: return null
    val jsonValue = jsonObjectAdapter.toJsonValue(value) ?: return null
    return modelAdapter.fromJsonValue(jsonValue)
  }
}
