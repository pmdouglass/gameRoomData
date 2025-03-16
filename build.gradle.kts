val kotlin_version: String by project
val logback_version: String by project
val ktor_version = "2.3.0"
val exposed_version = "0.41.1"
val postgresql_version = "42.6.0"

plugins {
    application
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("io.ktor.plugin") version "2.3.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Ktor core and Netty engine
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // Database (Exposed + PostgreSQL)
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:$postgresql_version")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.7")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    jar {
        enabled = false
    }
    shadowJar {
        archiveBaseName.set("gameRoom")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())
        manifest {
            attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
        }
    }
    build {
        dependsOn(shadowJar)
    }
}