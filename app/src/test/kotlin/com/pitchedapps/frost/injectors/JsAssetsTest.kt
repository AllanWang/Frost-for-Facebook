package com.pitchedapps.frost.injectors

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class JsAssetsTest {

    @Test
    fun verifyAssetsExist() {
        JsAssets.values().forEach { asset ->
            val file = File("src/web/assets/js/${asset.file}").absoluteFile
            assertTrue(file.exists(), "${asset.name} not found at ${file.path}")
        }
    }
}