# Troubleshooting

## `world/session.lock: already locked`

Another server process is already using the same world.

Fix:

1. Stop all Java server processes.
2. Make sure only one VioletCore jar is running.
3. Do not delete `session.lock` while a Java process is still alive.

## Engine Plugin rejected

Check the startup log. VioletCore validates:

```yaml
type: engine-plugin
target-server: VioletCore
target-version: 26.2
load-phase: pre-minecraft
```

If `strict-version-check` is true, wrong target versions are rejected.

## Engine Plugin conflict

If two Engine Plugins claim the same modification area, VioletCore rejects the second one.

Example:

```yaml
modifies:
  - entity-ticking
```

## `/violetcore reloadconfig` did not load a new jar

This is expected. Engine Plugins are restart-only. `reloadconfig` only reloads validation/config flags.

## SmartEntityTick changed gameplay

Keep these false unless you test carefully:

```yaml
affect-animals: false
affect-monsters: false
```
