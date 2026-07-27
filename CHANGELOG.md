# Changelog

## Repository cleanup - Core server only

### Changed

- Repository now focuses only on VioletCore server software, Engine Plugins, patches, examples, and development docs.
- Removed unrelated application/hosting files and documentation from the current tree.

## v0.6.0 - Engine Runtime Stats

### Added

- `/violetcore stats` command.
- `/violetcore resetstats` command.
- Core counters for EntityTickController checks and skipped entity ticks.
- Per-Engine-Plugin skip counters.
- Crash report details for entity tick checks/skips.

### Changed

- Release version updated to `26.2-v0.6.0`.

### Verified

- Built runnable server jar.
- Smoke tested startup with SmartEntityTick.
- Verified `/violetcore status`, `/violetcore stats`, `/violetcore resetstats`, and clean shutdown.

## v0.5.0 - Full Buildable Fork

### Added

- Repository now contains the full Purpur/Paperweight fork structure.
- `gradlew`, Gradle wrapper, root build files, `purpur-api`, and `purpur-server` are present.
- GitHub Actions now builds VioletCore with `applyAllPatches`, compile tasks, and `createBundlerJar`.
- Project versioning supports `violetcoreVersion = 0.5.0`, producing API version `26.2-v0.5.0`.

### Changed

- VioletCore is no longer only a patch bundle repository. It is now buildable from source like Purpur.
- Release docs point to v0.5.0.

### Verified

- Fresh clone from the full fork successfully ran `./gradlew applyAllPatches`.
- Fresh clone successfully built the runnable bundler jar.
- Smoke tested server startup with SmartEntityTick.
- Verified `/violetcore status`, `/violetcore config`, `/violetcore hooks`, and clean shutdown.

## v0.4.0 - Performance Core

### Added

- `engine-plugins.yml` core configuration generated on first boot.
- `/violetcore config` command.
- `/violetcore reloadconfig` command.
- Engine Plugin config options:
  - `engine-enabled`
  - `strict-version-check`
  - `warn-undeclared-entity-controller`
  - `fail-fast-on-plugin-error`
  - `debug-logging`
  - `disabled-plugins`
- Official optional Engine Plugin: `SmartEntityTick`.
- SmartEntityTick config file under its Engine Plugin data folder.

### Changed

- Engine Plugin loader can skip disabled plugin names from `engine-plugins.yml`.

### Verified

- Built runnable server jar.
- Smoke tested startup with SmartEntityTick.
- Verified `/violetcore status`, `/violetcore config`, `/violetcore reloadconfig`, `/violetcore engineplugins`, `/violetcore hooks`.
- Verified clean shutdown.

## v0.3.0 - Core Software Stabilization

### Added

- `/violetcore` command with:
  - `status`
  - `engineplugins`
  - `hooks`
  - `version`
- Engine Plugin lifecycle callbacks:
  - `onServerStarted`
  - `onServerStopping`
  - `onUnload`
- Engine Plugin context helpers:
  - `loadedEnginePlugins()`
  - `isModificationDeclared(String area)`
- Crash report details for hook counts and claimed areas.

### Changed

- Engine Plugin validation became stricter.
- Engine Plugin data directories respect custom `--engine-plugins-dir`.
- Entity tick controllers warn if the plugin did not declare `entity-ticking` in `modifies:`.

### Verified

- Built runnable server jar.
- Smoke tested startup with ExampleTickObserver.
- Verified `/violetcore status`, `/violetcore engineplugins`, and `/violetcore hooks`.
- Verified clean shutdown.

## v0.1.0 - Initial Prototype

### Added

- VioletCore 26.2 Purpur-based server jar.
- Engine Plugin loader.
- TickObserver hook.
- EntityTickController hook.
