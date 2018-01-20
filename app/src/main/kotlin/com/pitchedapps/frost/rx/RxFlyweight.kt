package com.pitchedapps.frost.rx

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Allan Wang on 07/01/18.
 *
 * Reactive flyweight to help deal with prolonged executions
 * Each call will output a [Single], which may be new if none exist or the old one is invalidated,
 * or reused if an old one is still valid
 *
 * Types:
 * T    input       argument for caller
 * C    condition   condition to check against for validity
 * R    response    response within reactive output
 */
abstract class RxFlyweight<in T : Any, C : Any, R : Any> {

    /**
     * Given an input emit the desired response
     * This will be executed in a separate thread
     */
    protected abstract fun call(input: T): R

    /**
     * Given an input and condition, check if
     * we may used cache data or if we need to make a new request
     * Return [true] to use cache, [false] otherwise
     */
    protected abstract fun validate(input: T, cond: C): Boolean

    /**
     * Given an input, create a new condition to be used
     * for future requests
     */
    protected abstract fun cache(input: T): C

    private val conditionals = mutableMapOf<T, C>()
    private val sources = mutableMapOf<T, Single<R>>()

    private val lock = Any()

    /**
     * Entry point to give an input a receive a [Single]
     * Note that the observer is not bound to any particular thread,
     * as it is dependent on [createNewSource]
     */
    operator fun invoke(input: T): Single<R> {
        synchronized(lock) {
            val source = sources[input]

            // update condition and retrieve old one
            val condition = conditionals.put(input, cache(input))

            // check to reuse observable
            if (source != null && condition != null && validate(input, condition))
                return source

            val newSource = createNewSource(input).cache().doOnError { sources.remove(input) }

            sources.put(input, newSource)
            return newSource
        }
    }

    /**
     * Open source creator
     * Result will then be created with [Single.cache]
     * If you don't have a need for cache,
     * you likely won't have a need for flyweights
     */
    open protected fun createNewSource(input: T): Single<R> =
            Single.fromCallable { call(input) }
                    .timeout(15, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())

    fun reset() {
        synchronized(lock) {
            sources.clear()
            conditionals.clear()
        }
    }

}