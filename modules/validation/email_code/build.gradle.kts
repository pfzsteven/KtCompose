plugins {
    kotlin("jvm")
}
val jdkVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.apache.commons:commons-email:1.5")
    compileOnly("org.json:json:20240303")
    implementation(project(":framework"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(jdkVersion.toInt())
}