plugins {
    kotlin("jvm")
}
val ktor_version: String by project
val jdkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":framework"))
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("io.ktor:ktor-server-core-jvm")
    compileOnly("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    compileOnly("org.json:json:20240303")
    implementation("commons-codec:commons-codec:1.16.1")
    implementation("io.jsonwebtoken:jjwt:0.12.5")

}

kotlin {
    jvmToolchain(jdkVersion.toInt())
}