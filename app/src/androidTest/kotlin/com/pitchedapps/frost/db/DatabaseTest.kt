package com.pitchedapps.frost.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.koin.error.NoBeanDefFoundException
import org.koin.standalone.get
import org.koin.test.KoinTest
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabaseTest : KoinTest {

    inline fun <reified T : Any> hasKoin() = hasKoin(T::class)

    fun <T : Any> hasKoin(klazz: KClass<T>): Boolean =
        try {
            get<T>(clazz = klazz)
            true
        } catch (e: NoBeanDefFoundException) {
            false
        }

    /**
     * Database and all daos should be loaded as components
     */
    @Test
    fun testKoins() {
        hasKoin<FrostDatabase>()
        val members = FrostDatabase::class.java.kotlin.functions.filter { it.name.endsWith("Dao") }
            .mapNotNull { it.returnType.classifier as? KClass<*> }
        assertTrue(members.isNotEmpty(), "Failed to find dao interfaces")
        val missingKoins = (members + FrostDatabase::class).filter { !hasKoin(it) }
        assertTrue(missingKoins.isEmpty(), "Missing koins: $missingKoins")
    }
}