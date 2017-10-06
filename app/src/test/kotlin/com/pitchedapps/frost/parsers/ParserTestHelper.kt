package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.Base
import java.net.URL
import java.nio.file.Paths

/**
 * Created by Allan Wang on 2017-10-06.
 */
 fun <T : Any> T.getResource(path: String): String? {
    Paths.get("src/test/resources/${path.trimStart('/')}")
    val resource: URL? = Base::class.java.classLoader.getResource(path)
    if (resource == null) {
        println("Resource at $path could not be found")
        return null
    }
    return resource.readText()
}