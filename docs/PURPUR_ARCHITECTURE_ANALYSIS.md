# Purpur Architecture Analysis for VioletCore

## What Purpur actually is

Purpur is a Paperweight patch-based fork of Paper. It is not maintained as a normal checked-in source tree. The root repository stores Gradle configuration and patch sets; generated source directories are produced by `./gradlew applyAllPatches`.

## Key root files

### `settings.gradle.kts`

Responsibilities:

- Requires a real Git clone.
- Defines subprojects:
  - `purpur-api`
  - `purpur-server`
- Computes Gradle project version from:
  - `mcVersion`
  - `channel`
  - `BUILD_NUMBER`

Default local version format:

```text
26.2.local-SNAPSHOT
```

CI build version format:

```text
26.2.build.<number>-stable
```

### `gradle.properties`

Important properties:

```properties
group = org.purpurmc.purpur
mcVersion = 26.2
apiVersion = 26.2
channel=STABLE
paperCommit = <fixed Paper commit>
```

`paperCommit` is the key upstream pin.

### root `build.gradle.kts`

Uses:

```kotlin
id("io.papermc.paperweight.patcher")
```

Responsibilities:

- Pull Paper at the configured `paperCommit`.
- Apply API patches.
- Patch `paper-api/build.gradle.kts` into `purpur-api/build.gradle.kts`.
- Patch `paper-server/build.gradle.kts` into `purpur-server/build.gradle.kts`.

## Patch layout

### API

```text
purpur-api/paper-patches/
```

Applies to generated `paper-api/`.

### Paper server

```text
purpur-server/paper-patches/
```

Applies to generated `paper-server/`.

### Minecraft/NMS

```text
purpur-server/minecraft-patches/
```

Applies to generated `purpur-server/src/minecraft/java` and resources.

## Development workflow

```bash
./gradlew applyAllPatches
```

Then edit generated repos:

```text
paper-api/
paper-server/
purpur-server/src/minecraft/java/
```

Then rebuild patches using relevant Gradle tasks:

```bash
./gradlew rebuildPaperApiPatches
./gradlew :purpur-server:rebuildPaperServerPatches
./gradlew :purpur-server:rebuildMinecraftPatches
```

## Important patch rule

Feature patches must be generated using full index hashes. Patches created with abbreviated index lines can fail during Paperweight application with errors like:

```text
sha1 information is lacking or useless
could not build fake ancestor
```

VioletCore patches must be regenerated through the Paperweight workflow or with full-index format patches.

## What VioletCore is missing compared to Purpur

1. Full buildable fork repository.
2. Gradle wrapper and Paperweight patcher as first-class project files.
3. Proper upstream pinning policy.
4. Clean patch workflow without duplicate API file/feature patches.
5. CI that actually builds the server jar.
6. Release automation with checksums.
7. API publishing for Engine Plugin developers.
8. Full-index patch generation discipline.
9. Upstream sync documentation.
10. Beta smoke tests.

## Migration conclusion

To become real server software, VioletCore must become a full Purpur-style patch repository, not just a bundle of patches and releases.

The correct path is:

```text
Purpur ver/26.2 full tree
+ VioletCore patches
+ VioletCore docs/examples/templates
+ Gradle versioning changes
+ CI/release pipeline
```

## Immediate blocker discovered

While starting the migration, the existing VioletCore server feature patch failed to apply cleanly on a fresh Purpur `ver/26.2` tree because the patch was generated against a different generated Paper state and used incomplete/abbreviated index context.

This confirms that the next required step is not adding more features, but regenerating VioletCore patches using the official Purpur/Paperweight workflow.
