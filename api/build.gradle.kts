plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    // module
    implementation(project(":domain"))

    // spring
    implementation(libs.spring.boot.starter.web)

    // h2 console (local profile only)
    implementation(libs.h2)

    // kotlin
    implementation(libs.jackson.module.kotlin)

    // test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.web.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.mockito.kotlin)
    testRuntimeOnly(libs.junit.platform.launcher)
}
