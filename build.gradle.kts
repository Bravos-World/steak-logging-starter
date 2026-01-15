plugins {
    java
    id("java-library")
    id("maven-publish")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.bravos.steak"
version = "1.0.6"
description = "logging-starter"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.projectreactor:reactor-core:3.8.1")
    implementation("org.springframework.boot:spring-boot-starter-aop:3.5.9")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("org.springframework.kafka:spring-kafka:4.0.1")
    implementation("com.github.Bravos-World:steak-utils:1.1.3")
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}