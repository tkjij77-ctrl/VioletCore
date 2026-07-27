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

dependencies {
    // Put purpur-api-26.2.local-SNAPSHOT.jar in libs/ or change this to your API dependency.
    compileOnly(files("libs/purpur-api-26.2.local-SNAPSHOT.jar"))
}

tasks.jar {
    archiveBaseName.set("ExampleEnginePlugin")
    from("src/main/resources")
}
