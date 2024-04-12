plugins {
    kotlin("jvm")
}
val ktor_version: String by project
val jdkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":framework"))
    implementation(project(":jwt"))
    implementation(project(":x-router"))
    compileOnly(project(":validation"))
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("io.ktor:ktor-server-core-jvm")
    compileOnly("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    compileOnly("io.ktor:ktor-network-tls-certificates:$ktor_version")
}

kotlin {
    jvmToolchain(jdkVersion.toInt())
}