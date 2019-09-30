import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java-library`
    `maven-publish`
    checkstyle
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.14.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:13.0")
    compile("org.jooq:jooq:3.12.0")
    compile("com.zaxxer:HikariCP:3.3.1")
    compile("org.flywaydb:flyway-core:6.0.0-beta2")
    compile("org.postgresql:postgresql:42.2.6")
    compile("io.reactivex.rxjava3:rxjava:3.0.0-RC2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_12
    targetCompatibility = JavaVersion.VERSION_12
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Replace tokens in yml files
val tokens = mapOf(
        "GROUP" to project.group,
        "NAME" to project.name,
        "NAME_LC" to project.name.toLowerCase(),
        "DESCRIPTION" to project.description,
        "VERSION" to project.version
)
tasks.withType<ProcessResources> {
    filesMatching("*.yml") {
        filter<ReplaceTokens>("tokens" to tokens)
    }
}
