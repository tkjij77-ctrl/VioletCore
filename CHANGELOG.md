# Changelog

## Repository cleanup - Core server only

### Changed

- Repository now focuses only on VioletCore server software, Engine Plugins, patches, examples, and development docs.
- Removed unrelated application/hosting files and documentation from the current tree.

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
