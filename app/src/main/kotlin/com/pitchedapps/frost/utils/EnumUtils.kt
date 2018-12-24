/*
 * Copyright 2018 Allan Wang
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
package com.pitchedapps.frost.utils

import android.content.Intent
import android.os.BaseBundle

/**
 * Created by Allan Wang on 29/12/17.
 *
 * Helper to set enum using its name rather than the serialized version
 * Name is used in case the enum is involved in persistent data, where updates may shift indices
 */
interface EnumBundle<E : Enum<E>> {

    val bundleContract: EnumBundleCompanion<E>

    val name: String

    val ordinal: Int

    fun put(intent: Intent) {
        intent.putExtra(bundleContract.argTag, name)
    }

    fun put(bundle: BaseBundle?) {
        bundle?.putString(bundleContract.argTag, name)
    }
}

interface EnumBundleCompanion<E : Enum<E>> {

    val argTag: String

    val values: Array<E>

    val valueMap: Map<String, E>

    operator fun get(name: String?) = if (name == null) null else valueMap[name]

    operator fun get(bundle: BaseBundle?) = get(bundle?.getString(argTag))

    operator fun get(intent: Intent?) = get(intent?.getStringExtra(argTag))
}

open class EnumCompanion<E : Enum<E>>(
    final override val argTag: String,
    final override val values: Array<E>
) : EnumBundleCompanion<E> {

    final override val valueMap: Map<String, E> = values.map { it.name to it }.toMap()

    final override fun get(name: String?) = super.get(name)

    final override fun get(bundle: BaseBundle?) = super.get(bundle)

    final override fun get(intent: Intent?) = super.get(intent)
}
