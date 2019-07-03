plugins {
    `kotlin-dsl`
}

group = "com.pitchedapps"

repositories {
    jcenter()
    maven("https://jitpack.io")
}

// Currently can't read properties from root project
// Reading it manually since it's simple
val rootProps =
    File(
        project.rootDir.let { if (it.name == "buildSrc") it.parent else it.absolutePath },
        "gradle.properties"
    )
val kau = rootProps.useLines {
    it.first { s -> s.startsWith("KAU=") }
}.substring(4).trim()

println("Using kau $kau")

gradlePlugin {
    plugins {
        register("frost-web-gen") {
            id = "frost"
            implementationClass = "FrostPlugin"
        }
    }
}

dependencies {
    implementation("ca.allanwang.kau:gradle-plugin:$kau")
}