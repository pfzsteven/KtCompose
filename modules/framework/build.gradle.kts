plugins {
    kotlin("jvm")
}
val ktor_version: String by project
val logback_version: String by project
val jdkVersion: String by project
val exposedVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}