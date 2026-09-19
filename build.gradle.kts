// =============================================================================
//  SkoolGuru — build.gradle.kts
//
//  STEP 5: Spring Boot 4.1.0 + Java 21 + Flyway.
//  Major jump: Spring Framework 7, Spring Security 7, Hibernate 7, Jakarta EE 11,
//  Jackson 3 by default, and Boot's module restructure (starter renames).
// =============================================================================

plugins {
    java
    id("org.springframework.boot") version "4.1.0"   // STEP 5: 3.5.16 -> 4.1.0
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.smsweb"
version = "0.0.1-SNAPSHOT"
description = "sms"

java {
    toolchain {
        // STEP 2: Java 17 -> 21 LTS. Spring Boot 3.2.7 fully supports Java 21.
        // Nothing else changes in this commit. If this runs, Java 21 is proven
        // and stops being a suspect when we move to Spring Boot 4.1.
        languageVersion = JavaLanguageVersion.of(21)
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
    // RENAMED in Boot 4: spring-boot-starter-web -> spring-boot-starter-webmvc.
    // (If something turns out to be missing, "spring-boot-starter-classic" restores
    // the whole pre-modularisation classpath as a temporary fallback.)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")


    // ── Jackson ──────────────────────────────────────────────────────────────
    // No bridge needed. Boot 4 uses Jackson 3, and Jackson 3 deliberately KEPT the
    // annotations in com.fasterxml.jackson.annotation ("jackson-annotations: 2.x
    // version still used with 3.x, so no group-id/Java package change"). All 47
    // Jackson imports in this codebase are annotations - @JsonIgnore(27),
    // @JsonBackReference(8), @JsonProperty(4), @JsonManagedReference(4),
    // @JsonIgnoreProperties(3), @JsonFormat(1) - and nothing uses ObjectMapper,
    // JsonNode or anything else from databind/core, which DID move to tools.jackson.
    // So the cycle-breakers on FeeSubmission etc. keep working as-is.

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

    // ── Hashids — obfuscates numeric ids (fee-receipt ids) in URLs so they
    // can't be casually guessed/incremented. NOT an access-control mechanism
    // by itself (see ReceiptIdCodec's javadoc) - that's the school-ownership
    // check in FeeSubmissionService#getFeeReceiptData.
    implementation("org.hashids:hashids:1.0.3")

    // ── Firebase Admin SDK — push notifications ──────────────────────────────
    // Initialised from a service-account JSON given by app.firebase.credentials.path
    // (see FirebaseConfig). That file must never be committed.
    implementation("com.google.firebase:firebase-admin:9.4.1")

    // ── Database driver ──────────────────────────────────────────────────────
    runtimeOnly("com.mysql:mysql-connector-j")

    // ── Flyway — versioned schema migrations ─────────────────────────────────
    // STEP 3. Replaces `ddl-auto=update`, which let Hibernate alter production
    // tables straight from entity changes with no version history and no way to
    // roll back. The last bootRun did exactly that: it dropped and recreated
    // unique constraints on month_mapping and sibling_discount at startup.
    // From here every schema change is a numbered, reviewed SQL file.
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

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
    // Serve templates/static straight from src/main/resources instead of the
    // copies under build/resources/main. Without this, editing a .html or .css
    // has NO effect on the running app until `processResources` re-runs - which
    // is confusing, because under Maven + IntelliJ that copy happened for you.
    // With this, DevTools picks up template edits on the next page refresh.
    sourceResources(sourceSets["main"])

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
    // Show exactly what is deprecated. Anything deprecated in the 3.5 line is a
    // strong candidate for REMOVAL in 4.x - e.g. AntPathRequestMatcher in
    // WebSecurityConfig. Fixing them here means they are not tangled up with
    // everything else when we jump to 4.1.
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
