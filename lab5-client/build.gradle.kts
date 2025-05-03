plugins {
    application
}

group = "ru.alspace"
version = "1.0-SNAPSHOT"

application {
    mainClass = "ru.alspace.Main"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":lab5-common"))

    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    runtimeOnly(libs.log4j.slf4j2.impl)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
