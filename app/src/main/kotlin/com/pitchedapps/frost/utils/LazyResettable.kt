package com.pitchedapps.frost.utils

import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Created by Allan Wang on 2017-05-30.
 *
 * Lazy delegate that can be invalidated if needed
 * https://stackoverflow.com/a/37294840/4407321
 */
private object UNINITIALIZED

fun <T : Any> lazyResettable(initializer: () -> T): LazyResettable<T> = LazyResettable<T>(initializer)

class LazyResettable<T : Any>(private val initializer: () -> T, lock: Any? = null) : Lazy<T>, Serializable {
    @Volatile private var _value: Any = UNINITIALIZED
    private val lock = lock ?: this

    fun invalidate() {
        _value = UNINITIALIZED
    }

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED)
                @Suppress("UNCHECKED_CAST")
                return _v1 as T

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED) {
                    @Suppress("UNCHECKED_CAST")
                    _v2 as T
                } else {
                    val typedValue = initializer()
                    _value = typedValue
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    operator fun setValue(any: Any, property: KProperty<*>, t: T) {
        _value = t
    }
}