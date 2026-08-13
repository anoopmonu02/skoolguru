// =============================================================================
//  SkoolGuru — build.gradle.kts
//
//  STEP 1 of the migration: build tool ONLY.
//  Spring Boot stays 3.2.7, Java stays 17, every dependency version is
//  identical to the pom.xml it replaces. If this build produces a working
//  app, Gradle is proven and is no longer a suspect for anything that
//  follows. Do not change versions in this commit.
// =============================================================================

plugins {
    java
    id("org.springframework.boot") version "3.2.7"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.smsweb"
version = "0.0.1-SNAPSHOT"
description = "sms"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)   // was <java.version>17</java.version>
    }
}

// Lets `compileOnly` dependencies (Lombok) also be visible to the annotation
// processor path. Standard Spring Initializr boilerplate — keep it.
configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories {
    mavenCentral()
}

dependencies {

    // ── Spring Boot starters (versions managed by the BOM) ───────────────────
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Was declared explicitly in the pom. It is already pulled in by
    // starter-web, so it is redundant — but this commit changes the build tool
    // and NOTHING else, so it stays. Remove it in a later commit.
    implementation("org.springframework:spring-webmvc")

    // ── Thymeleaf extras ─────────────────────────────────────────────────────
    // Version deliberately OMITTED — Spring Boot's BOM manages this artifact
    // (3.1.2.RELEASE under Boot 3.2.7). The pom pinned 3.1.0.RELEASE, whose own
    // POM imports spring-security-bom:6.0.0-RC2 — a release candidate that was
    // never published to Maven Central. That build only ever worked because the
    // jar was already cached in ~/.m2. Letting the BOM choose is both resolvable
    // and self-updating when we move to Boot 4.1.
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    // #temporals for LocalDate in templates (dob, AcademicYear, Holiday)
    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time:3.0.4.RELEASE")

    // ── Excel import/export ──────────────────────────────────────────────────
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // ── JWT for the mobile API ───────────────────────────────────────────────
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // ── Firebase Admin SDK — push notifications ──────────────────────────────
    // Initialised from a service-account JSON given by app.firebase.credentials.path
    // (see FirebaseConfig). That file must never be committed.
    implementation("com.google.firebase:firebase-admin:9.4.1")

    // ── Database driver ──────────────────────────────────────────────────────
    runtimeOnly("com.mysql:mysql-connector-j")

    // ── Lombok ───────────────────────────────────────────────────────────────
    // BOTH lines are required. With only compileOnly the annotation processor
    // never runs and every entity fails with "cannot find symbol: method getX()".
    // The pom's <excludes> for Lombok in the boot plugin is unnecessary here —
    // compileOnly already keeps Lombok out of the produced jar.
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // ── Dev / test ───────────────────────────────────────────────────────────
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// pom had <finalName>sms</finalName> -> produces build/libs/sms.jar
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "sms.jar"
}

// ── Local dev environment for `./gradlew bootRun` ────────────────────────────
// application.properties reads secrets from env vars (SPRING_DATASOURCE_PASSWORD,
// JWT_SECRET, MAIL_PASSWORD). Those were previously supplied by the IntelliJ Run
// Configuration, so a plain terminal `bootRun` has none of them and MySQL rejects
// the connection. Put them in `local.env.properties` (git-ignored) instead:
//
//     SPRING_DATASOURCE_PASSWORD=yourpassword
//     JWT_SECRET=some-long-dev-only-secret-at-least-32-characters
//     MAIL_PASSWORD=whatever
//
// Never commit that file. Real secrets stay out of source control.
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val envFile = rootProject.file("local.env.properties")
    if (envFile.exists()) {
        envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .forEach { line ->
                val i = line.indexOf('=')
                environment(line.substring(0, i).trim(), line.substring(i + 1).trim())
            }
        logger.lifecycle("bootRun: loaded local.env.properties")
    } else {
        logger.lifecycle("bootRun: no local.env.properties - using shell env vars")
    }
}

// Gradle also builds a "-plain.jar" (classes only, not runnable). Maven never
// did. Disabled so build/libs contains exactly one jar — otherwise a Dockerfile
// doing `COPY build/libs/*.jar` can silently pick the non-runnable one.
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"              // was project.build.sourceEncoding
    options.compilerArgs.add("-parameters") // Spring needs param names for @PathVariable etc.
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
