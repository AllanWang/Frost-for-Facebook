plugins {
    `kotlin-dsl`
}

group = "com.pitchedapps"

repositories {
    jcenter()
    maven("https://jitpack.io")
}

val KAU: String = "db3b6c0"

dependencies {
    implementation("ca.allanwang.kau:gradle-plugin:$KAU")
}