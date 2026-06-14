plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.moloch"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.build {
    dependsOn("shadowJar")
}