import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
//    groovy
//    idea
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

//sourceSets {
//    main {
//        withConvention(GroovySourceSet::class) {
//            groovy.srcDir("src/main/groovy")
//        }
//    }
//}

dependencies {
    implementation("ca.allanwang.kau:gradle-plugin:$kau")
    implementation("com.moowork.gradle:gradle-node-plugin:1.3.1")
}

//val compileGroovy = tasks.withType<GroovyCompile>().first()
//val compileKotlin = tasks.withType<KotlinCompile>().first()
//
//compileGroovy.dependsOn.remove(compileKotlin)
//compileKotlin.dependsOn(compileGroovy)
//compileKotlin.classpath += files(compileGroovy.destinationDir)
