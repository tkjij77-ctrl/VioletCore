# Development

## Base

VioletCore is based on:

```text
PurpurMC/Purpur branch: ver/26.2
```

## Requirements

- JDK 25
- Git
- Linux/macOS shell recommended

## Build from upstream + patches

```bash
git clone --branch ver/26.2 --single-branch https://github.com/PurpurMC/Purpur.git VioletCore
cd VioletCore
```

Copy this repository's `patches/` into the same relative locations in the clone.

Then:

```bash
./gradlew applyAllPatches
./gradlew :purpur-server:createBundlerJar -x test
```

Output:

```text
purpur-server/build/libs/purpur-bundler-26.2.local-SNAPSHOT.jar
```

## Engine Plugin template

Start from:

```text
templates/engine-plugin-template/
```

## Release checklist

- Compile server.
- Build runnable jar.
- Start without Engine Plugins.
- Start with Example Engine Plugin.
- Start with SmartEntityTick.
- Verify `/violetcore status`.
- Verify `/violetcore hooks`.
- Verify clean shutdown.
