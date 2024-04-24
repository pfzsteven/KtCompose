val ktor_version: String by project
val kotlin_version: String by project
val jdkVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.9"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    // 配置依赖
    implementation(project(":engine"))
    implementation(project(":framework"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-tomcat-jvm")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}