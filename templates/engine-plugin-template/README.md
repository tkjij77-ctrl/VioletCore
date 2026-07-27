# VioletCore Engine Plugin Template

This template creates a simple Engine Plugin for VioletCore 26.2.

## Requirements

- JDK 25
- Gradle
- VioletCore API jar from the latest release

## Setup

Create the `libs/` folder and download the API jar from the VioletCore release:

```text
libs/VioletCore-API-26.2-v0.9.0.jar
```

## Build

```bash
./gradlew build
```

Output:

```text
build/libs/ExampleEnginePlugin-1.0.0.jar
```

Copy the jar to your server:

```text
engine-plugins/
```

## Metadata

Edit:

```text
src/main/resources/engine-plugin.yml
```

Required fields:

```yaml
type: engine-plugin
target-server: VioletCore
target-version: 26.2
load-phase: pre-minecraft
```
