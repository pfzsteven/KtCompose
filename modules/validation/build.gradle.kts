plugins {
    kotlin("jvm")
}

val ktor_version: String by project
val kotlin_version: String by project
val jdkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":framework"))
    implementation(project(":email_code"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}