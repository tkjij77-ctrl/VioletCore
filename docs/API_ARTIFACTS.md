# VioletCore API Artifacts

Starting with `v0.9.0`, VioletCore releases include an API jar for Engine Plugin developers.

## Release asset

```text
VioletCore-API-26.2-v0.9.0.jar
```

Use this jar as `compileOnly` when building Engine Plugins.

## Example Gradle dependency

```kotlin
val violetCoreApiJar = file("libs/VioletCore-API-26.2-v0.9.0.jar")

dependencies {
    compileOnly(files(violetCoreApiJar))
}
```

## Why compileOnly?

The API classes are provided by the VioletCore server at runtime. Engine Plugins should not shade or bundle VioletCore API classes.

## Compatibility

Engine Plugin jars should set:

```yaml
target-server: VioletCore
target-version: 26.2
```

VioletCore rejects wrong target versions when `strict-version-check` is enabled.
