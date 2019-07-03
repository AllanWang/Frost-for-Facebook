plugins {
    `kotlin-dsl`
}

group = "com.pitchedapps"

repositories {
    jcenter()
    maven("https://jitpack.io")
}

var isRoot = false
// Currently can't read properties from root project
// Reading it manually since it's simple
val rootProps =
    File(
        project.rootDir.let {
            if (it.name == "buildSrc") {
                it.parent
            } else {
                isRoot = true
                it.absolutePath
            }
        },
        "gradle.properties"
    )
val kau = rootProps.useLines {
    it.first { s -> s.startsWith("KAU=") }
}.substring(4).trim()

if (isRoot) {
    println("Using kau $kau")
}

dependencies {
    implementation("ca.allanwang.kau:gradle-plugin:$kau")
}