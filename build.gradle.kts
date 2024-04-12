val ktor_version: String by project
val kotlin_version: String by project
val jdkVersion: String by project

plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.9"
}

group = "org.beautyspace.ktc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.ktcompose.main.Application")
}

allprojects {
    subprojects {
        println(">>Submodule:[${project.name}]")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-tomcat-jvm")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")
    // 配置依赖
    implementation(project(":engine"))
    implementation(project(":framework")) // 必须
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}