package com.pitchedapps.frost.services

import org.junit.Test
import kotlin.test.assertNotNull

/**
 * Created by Allan Wang on 07/04/18.
 */
class UpdateServiceTest {

    @Test
    fun getRelease() {
        val release = UpdateManager.getLatestGithubRelease()
        assertNotNull(release)
        assertNotNull(release!!.apk, "Apk not uploaded for $release")
    }

}