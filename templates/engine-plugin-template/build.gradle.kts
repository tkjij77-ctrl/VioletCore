plugins {
    java
}

group = "dev.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

val violetCoreApiJar = file("libs/VioletCore-API-26.2-v0.9.0.jar")

dependencies {
    if (violetCoreApiJar.exists()) {
        compileOnly(files(violetCoreApiJar))
    } else {
        throw GradleException("Missing ${violetCoreApiJar}. Download it from the VioletCore release and place it in libs/.")
    }
}

tasks.jar {
    archiveBaseName.set("ExampleEnginePlugin")
    from("src/main/resources")
}
