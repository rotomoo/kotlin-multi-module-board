plugins {
    `java-library`
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    // spring
    api(libs.spring.boot.starter.data.jpa)

    // kotlin
    api(libs.kotlin.reflect)

    // database
    runtimeOnly(libs.h2)
    runtimeOnly(libs.mysql)

    // test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.junit.platform.launcher)
}
