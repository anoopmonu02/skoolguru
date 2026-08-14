// Lets Gradle download a matching JDK automatically when the toolchain version
// requested in build.gradle.kts (Java 21) isn't installed locally. Without this,
// Gradle fails with "No matching toolchains found" and you'd have to install a
// JDK by hand on every machine and CI runner.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "sms"
