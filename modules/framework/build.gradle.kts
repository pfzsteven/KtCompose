plugins {
    kotlin("jvm")
}
val ktor_version: String by project
val logback_version: String by project
val jdkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("org.json:json:20240303")
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}