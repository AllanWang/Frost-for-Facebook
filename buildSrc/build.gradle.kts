plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

val KAU: String = "df94b2f"

dependencies {
    implementation("ca.allanwang.kau:gradle-plugin:$KAU")
}