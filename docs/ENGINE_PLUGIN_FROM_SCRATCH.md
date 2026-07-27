# Creating an Engine Plugin from Scratch

This guide explains how to create a VioletCore Engine Plugin without using the template.

## 1. Create project structure

```text
my-engine-plugin/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── java/dev/example/MyEnginePlugin.java
    └── resources/engine-plugin.yml
```

## 2. Add the VioletCore API jar

Download from the VioletCore release:

```text
VioletCore-API-26.2-v0.9.0.jar
```

Place it in:

```text
libs/VioletCore-API-26.2-v0.9.0.jar
```

## 3. build.gradle.kts

```kotlin
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
    compileOnly(files("libs/VioletCore-API-26.2-v0.9.0.jar"))
}

tasks.jar {
    from("src/main/resources")
}
```

## 4. engine-plugin.yml

```yaml
name: MyEnginePlugin
version: 1.0.0
main: dev.example.MyEnginePlugin
type: engine-plugin
target-server: VioletCore
target-version: 26.2
load-phase: pre-minecraft
reloadable: false
modifies:
  - tick-observer-demo
conflicts: []
mixin-configs: []
```

## 5. Java entrypoint

```java
package dev.example;

import io.violetmc.violetcore.engine.api.EnginePlugin;
import io.violetmc.violetcore.engine.api.EnginePluginContext;
import io.violetmc.violetcore.engine.api.TickObserver;

public final class MyEnginePlugin implements EnginePlugin {
    @Override
    public void onLoad(EnginePluginContext context) {
        context.logger().info("Loaded " + context.description().displayName());
        context.registerTickObserver(new TickObserver() {
            @Override
            public void onServerTickEnd(int tick, double mspt, long remainingNanos) {
                if (tick % 100 == 0) {
                    context.logger().info("tick=" + tick + ", mspt=" + mspt);
                }
            }
        });
    }
}
```

## 6. Build

```bash
./gradlew build
```

## 7. Install

Copy the plugin jar to:

```text
engine-plugins/
```

Restart the server.

## 8. Verify

Use:

```text
/violetcore engineplugins
/violetcore hooks
/violetcore stats
```

## Safety rules

- Engine Plugins are restart-only.
- Declare every internal area you modify in `modifies:`.
- Do not touch chunk saving, player inventory transactions, or networking unless you fully understand the risk.
