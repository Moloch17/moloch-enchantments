plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.moloch"
version = "1.1.0"

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

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

// Build the distributable resource pack zip (upload its output to your host).
// Output: build/generated/resourcepack/pack.zip
tasks.register<Zip>("packZip") {
    from("resourcepack")
    archiveFileName.set("pack.zip")
    destinationDirectory.set(layout.buildDirectory.dir("generated/resourcepack"))
}

tasks.build {
    dependsOn("shadowJar", "packZip")
}