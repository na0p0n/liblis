plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.flywaydb.flyway") version "11.20.2"
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.github.jakemarsden.git-hooks") version "0.0.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

gitHooks {
    hooks = mapOf("pre-commit" to "ktlintCheck detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}
ktlint {
    version.set("1.2.1")
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }

    filter {
        exclude { it.file.path.contains("generated/") }
    }
}

group = "net.naoponju"
version = "0.0.1-SNAPSHOT"
description = "Manage Book System"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

flyway {
    baselineOnMigrate = true
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
