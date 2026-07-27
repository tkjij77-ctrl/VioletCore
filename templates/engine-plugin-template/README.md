# VioletCore Engine Plugin Template

This template creates a simple Engine Plugin for VioletCore 26.2.

## Requirements

- JDK 25
- Gradle
- A copy of `purpur-api-26.2.local-SNAPSHOT.jar` in `libs/`

## Build

```bash
./gradlew build
```

Output:

```text
build/libs/ExampleEnginePlugin-1.0.0.jar
```

Copy the jar to:

```text
engine-plugins/
```

## Metadata

Edit:

```text
src/main/resources/engine-plugin.yml
```

Make sure:

```yaml
target-server: VioletCore
target-version: 26.2
```
