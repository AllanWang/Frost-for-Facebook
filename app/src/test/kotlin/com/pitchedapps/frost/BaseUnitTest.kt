package com.pitchedapps.frost

import android.content.Context
import android.os.Build
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import timber.log.Timber

/**
 * Created by Allan Wang on 2017-05-30.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP),
        assetDir = "build/intermediates/classes/test/")
abstract class BaseUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowTestRule.create()

    val context: Context
        get() = RuntimeEnvironment.application

    init {
        Timber.plant(TestTree())
    }

    internal class TestTree : Timber.Tree() {
        override fun log(priority: Int, tag: String, message: String, t: Throwable?) {
            System.out.println("$tag-$priority: $message")
        }
    }
}
