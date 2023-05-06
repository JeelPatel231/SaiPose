plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project.libs.okhttp)
    api(project.libs.dev.brahmkshatriya.nicehttp)
    api(project.libs.kotlinx.serialization.json)
    api(project.libs.kotlin.reflect)
}