package com.pitchedapps.frost.injectors

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class CssAssetsTest {

    @Test
    fun verifyAssetsExist() {
        CssAssets.values().forEach { asset ->
            val file = File("src/web/assets/css/${asset.folder}/${asset.file}").absoluteFile
            assertTrue(file.exists(), "${asset.name} not found at ${file.path}")
        }
    }
}