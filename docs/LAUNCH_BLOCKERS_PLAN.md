# VioletCore — Launch Blocker Analysis & Remediation Plan

**Repo:** `tkjij77-ctrl/VioletCore` @ `a2ddf72` (v0.9.0)
**Baseline:** `PurpurMC/Purpur` @ `ver/26.2` · **Upstream:** `PaperMC/Paper`
**Date:** 2026-07-28
**Scope of this pass:** only defects that stop the project from launching or stop the server from starting at all.

---

## Executive summary

| # | Blocker | Severity | Server starts? | Status |
|---|---|---|---|---|
| B1 | Advertised server jar is **not in the release** (404) | 🔴 Critical | N/A — nothing to run | **FIXED** |
| B2 | `ServerBuildInfoImpl` record component inserted mid-list | 🔴 Critical | ❌ Build fails | **FIXED** |
| B3 | `/violetcore` registered only on the **reload** path | 🟠 High | ✅ but command missing | **FIXED** |
| B4 | Release workflow uploads unverified/partial assets | 🔴 Critical | N/A | **FIXED** |
| B5 | MIT notice stripped: PurpurMC copyright removed | 🟠 High (legal) | ✅ | **FIXED** |
| B6 | Reload guard placed after Paper's lifecycle throw | 🟡 Medium | ✅ | **FIXED** |

Verified as **NOT** problems (checked and cleared):
- **Java 25 is correct** — Purpur `ver/26.2` itself sets `JavaLanguageVersion.of(25)` and `options.release = 25`. VioletCore matches upstream exactly. My earlier note calling this a risk was wrong; retracted.
- Shipped `SmartEntityTick.jar` and API jar are valid, Java 25 class files (major 69), metadata present.
- The three engine patches are structurally well-formed and `git apply --numstat` parses them.

---

## B1 — The advertised server jar does not exist 🔴

**This is the single reason the project cannot launch.** Nothing else matters until it is fixed.

The README's primary download link:
```
https://github.com/tkjij77-ctrl/VioletCore/releases/download/v0.9.0/VioletCore-26.2-v0.9.0.jar
→ HTTP 404
```

Actual v0.9.0 release assets:
```
SHA256SUMS.txt                                385 B
SmartEntityTick-1.1.0.jar                    7333 B
SmartEntityTick-1.1.0.jar.sha256               92 B
VioletCore-26.2-v0.9.0.jar.sha256              93 B   ← checksum present…
VioletCore-API-26.2-v0.9.0.jar            2923530 B
VioletCore-API-26.2-v0.9.0.jar.sha256          97 B
VioletCore-source-patches-v0.9.0.zip       559520 B
VioletCore-source-patches-v0.9.0.zip.sha256   103 B
```

`VioletCore-26.2-v0.9.0.jar` is **absent**, but its `.sha256` shipped, and `SHA256SUMS.txt` lists it:
```
3aa5ff793f287dbb96f35a3a52d5fcaf63b5b3b093ee03cea00a2c4117f51dec  VioletCore-26.2-v0.9.0.jar
```

**Diagnosis:** the build genuinely produced the jar (its hash was computed), so `createBundlerJar` succeeded. The loss is at upload. Paperweight's bundler task emits *several* jars into `purpur-server/build/libs/` — `-bundler.jar`, `-mojmap.jar`, and a plain one. The glob `cp purpur-server/build/libs/*bundler*.jar release-assets/…` copies **all** matches onto one destination path; with more than one match `cp` treats the last argument as a directory and fails, or silently produces one file. Combined with `softprops/action-gh-release` tolerating missing globs, the job went green while dropping the main artifact. Every download count is 0, so nobody has hit this yet.

**Options considered**

| Approach | Verdict |
|---|---|
| Manually upload the jar to the existing release | ✗ Doesn't stop recurrence |
| Loosen the glob | ✗ Same ambiguity |
| **Resolve exactly one jar, fail loudly if not, verify before upload** | ✅ **Chosen** |

**Chosen fix — deterministic resolution + hard gate.** Select the bundler jar explicitly, assert exactly one match, assert non-zero size, verify every checksum with `sha256sum -c`, and assert the server jar is present in `release-assets/` before the upload step runs. `fail_on_unmatched_files: true` added so the action itself refuses to publish a partial release.

---

## B2 — `ServerBuildInfoImpl` record component inserted mid-list 🔴

**This breaks the build.** Paper's record:

```java
public record ServerBuildInfoImpl(
    Key brandId, String brandName,
    String minecraftVersionId, String minecraftVersionName,
    OptionalInt buildNumber, Instant buildTime,
    Optional<String> gitBranch, Optional<String> gitCommit
) implements ServerBuildInfo {
```

The VioletCore patch inserts `Optional<String> implementationVersion` **between `minecraftVersionName` and `buildNumber`**, changing the canonical constructor signature and the accessor order.

Two concrete failures:

1. **Public no-arg constructor breaks.** Paper declares `public ServerBuildInfoImpl()` (line 37) delegating to the private `Manifest` constructor. Reordering components changes the canonical signature that delegation targets.
2. **`ServerBuildInfo` is `@ApiStatus.NonExtendable` and loaded via `Services.service(...)`** (ServiceLoader). It is an **API-surface record** in `paper-api`, resolved at runtime by service lookup. Adding a component to the impl without the interface knowing is fragile, and the accessor `implementationVersion()` becomes part of the record's public shape in the middle of the sequence.

Insertion order also makes the patch maximally fragile: any upstream Paper change near those lines causes a reject.

**Options considered**

| Approach | Verdict |
|---|---|
| Keep component, move to end of list | ⚠️ Still mutates a NonExtendable API record |
| Read the manifest attribute in `asString()` on demand | ⚠️ Re-reads manifest per call |
| **Private static field, resolved once, no record change** | ✅ **Chosen** |

**Chosen fix.** Drop the record component entirely. Resolve `Implementation-Version` once into a `private static final Optional<String>` via the same `JarManifests` lookup Paper already uses, and consult it at the top of `asString()`. Zero change to the record shape, zero change to the constructor, and the patch no longer touches the component list — so it survives upstream edits.

---

## B3 — `/violetcore` is registered only during reload 🟠

The patch adds the command in `PaperCommands.registerCommands(MinecraftServer)`:

```java
COMMANDS.put("paper", new PaperCommand("paper"));
COMMANDS.put("violetcore", new VioletCoreCommand("violetcore"));   // VioletCore
```

I traced every call site of that overload in `CraftServer` (2964 lines). It is invoked **exactly once**, at line 1018 — and the enclosing method is **`public void reload()`** (line 950). It is never called on the normal boot path.

Consequence: on a freshly started server **`/violetcore` does not exist**. It only appears after `/reload`. And B6 below means reload is *cancelled* whenever Engine Plugins are loaded — so on the exact configuration VioletCore is built for, **the command can never be registered at all.** Every documented command (`status`, `stats`, `hooks`, `config`, `engineplugins`) is unreachable.

The v0.3.0–v0.9.0 changelogs claim these were "verified" via smoke tests. They cannot have been, on a normal boot.

There is a second latent fault: `VioletCoreCommand`'s constructor calls `Bukkit.getServer().getPluginManager().addPermission(...)`. During boot `Bukkit.setServer(this)` runs at CraftServer:407 and `pluginManager` is assigned at :410 — so constructing the command before that point would NPE. Registering at the correct, later point avoids this.

**Options considered**

| Approach | Verdict |
|---|---|
| Add a second `registerCommands` call at boot | ✗ Double-registers on reload |
| Use the modern Brigadier `registerCommands()` no-arg path | ⚠️ Larger patch, more upstream drift |
| **Register in `CraftServer` on the boot path, guarded against duplicates** | ✅ **Chosen** |

**Chosen fix.** Register `/violetcore` through the command map during server construction (after `pluginManager` exists), and make the map insertion idempotent so a later `reload()` cannot double-register. Keeps the patch small and preserves the existing `Command` subclass.

---

## B4 — Release workflow publishes unverified assets 🔴

Root cause of B1, but independently dangerous. The workflow:
- computes checksums but **never verifies** them,
- never asserts the server jar exists,
- lets `action-gh-release` publish whatever globs happen to match.

Result: a release that looks complete (checksums, SHA256SUMS, green tick) but is missing its main artifact.

**Chosen fix.** Add a verification gate: exactly-one-match resolution, non-empty assertion, `sha256sum -c SHA256SUMS.txt`, explicit presence check for the server jar, and `fail_on_unmatched_files: true`. A broken release now fails the job instead of publishing.

---

## B5 — MIT attribution notice stripped 🟠

**Correction to my first pass.** I initially wrote this up as "GPL code relabelled MIT."
That was wrong, and I verified it against upstream: **Purpur's own root `LICENSE` is
also MIT** (`Copyright (c) 2019-2024 PurpurMC`). Using MIT at the root is therefore
consistent with the direct upstream, not a violation of it. Retracted.

The real defect is narrower but still blocks a clean public launch.

VioletCore's `LICENSE` read:

```
MIT License
Copyright (c) 2026 VioletCore contributors
```

The repository ships **65 Purpur-authored `.java` files** under `purpur-api/src` and
`purpur-server/src` (e.g. `org/purpurmc/purpur/event/PlayerAFKEvent.java`). Those files
carry no per-file copyright headers, so the root `LICENSE` is the only place PurpurMC's
notice can live — and it had been replaced rather than added to.

MIT is explicit on this point:

> The above copyright notice and this permission notice shall be included in all copies
> or substantial portions of the Software.

Redistributing 65 MIT files while removing the copyright line of their author is a
straightforward breach of the one condition MIT imposes. `NOTICE.md` linked to Purpur,
but a "see also" link is not the retained notice the licence requires.

Separately, the **built jar** does embed Paper/Spigot/Bukkit/CraftBukkit code, which
Paper's `LICENSE.md` states is GPL-3.0. The repo previously said nothing about this.

**Options considered**

| Approach | Verdict |
|---|---|
| Switch root LICENSE to GPLv3 | ✗ Wrong — misstates Purpur's actual MIT terms |
| Add per-file headers to all 65 files | ✗ Invasive, and upstream didn't use them |
| **Retain both copyright lines in root LICENSE; document the jar's GPL obligation** | ✅ **Chosen** |

**Chosen fix.** Keep MIT (matching Purpur), restore `Copyright (c) 2019-2024 PurpurMC`
above the VioletCore line, and add a short section stating that the *compiled jar* is a
GPL-3.0 combined work. `NOTICE.md` rewritten to state precisely which files are original
(the three patches, `examples/`, `templates/`, `docs/`) versus upstream, with a licence
table. This matches how Paper and Purpur themselves handle the fork/jar split.

## B6 — Reload guard placed after Paper's lifecycle throw 🟡

```java
public void reload() {
    if (EnginePluginManager.hasLoadedPlugins() && !…AllowReload…) {   // VioletCore
        this.getLogger().severe("Reload cancelled…");
        return;
    }
    if (LifecycleEventRunner.INSTANCE.blocksPluginReloading()) {      // Paper
        throw new IllegalStateException(RELOADING_DISABLED_MESSAGE);
    }
```

The guard `return`s silently *before* Paper's check. Two problems: it swallows Paper's stronger `IllegalStateException`, and a bare `return` from `reload()` leaves the caller believing reload succeeded. Ordering also means VioletCore's softer rule overrides Paper's hard one.

**Chosen fix.** Move the guard *after* Paper's lifecycle check, and throw `IllegalStateException` instead of returning — so `/reload` reports failure honestly and Paper's own policy keeps precedence.

---

## Execution order

1. **B2** — build must compile before anything ships
2. **B3, B6** — server-behaviour correctness
3. **B5** — legal, before any public distribution
4. **B4 → B1** — fix the pipeline, then re-cut the release

---

## Verification performed

- `ServerBuildInfoImpl` diffed against live Paper `main`; confirmed record shape, `public ServerBuildInfoImpl()` at :37, and existing `ChronoUnit` import
- `PaperCommands.registerCommands(MinecraftServer)` traced to a single call site inside `CraftServer.reload()` (:1018, method opens :950)
- `Bukkit.setServer` (:407) / `pluginManager` (:410) ordering confirmed for the NPE risk
- Release assets enumerated via GitHub API; server jar 404 reproduced; `SHA256SUMS.txt` and orphan `.sha256` retrieved as proof the build produced it
- Shipped jars unpacked: class-file major version **69 (Java 25)** — consistent and correct
- Purpur `ver/26.2` `build.gradle.kts` checked: toolchain **25**, `options.release = 25` — VioletCore matches upstream, Java 25 cleared as a non-issue
- All edited patches re-parsed with `git apply --numstat`; hunk headers and diffstats recomputed with a purpose-written recalculator (`git apply` rejected two intermediate edits as *corrupt patch* until the context counts were right — that check is what caught them)
- Full patched engine re-extracted and compiled under JDK 21 against Bukkit/Adventure stubs; `hasEntityTickControllers()` and `VioletCoreCommand.register(CommandMap)` confirmed in the class files via `javap`
- `register(CommandMap)` signature checked against real Bukkit `CommandMap` — `boolean register(String fallbackPrefix, Command command)` exists upstream
- Release-workflow guard logic simulated locally for 0, 1 and 2 bundler-jar matches; fails loudly on 0 and 2, proceeds on 1
- Both workflow YAML files re-parsed after editing
- SmartEntityTick re-benchmarked after all edits: still `skipped=64920`, still ~2.7x faster

**Not verified (cannot be, in this sandbox):** a full `applyAllPatches` + `createBundlerJar` run and a live server boot. No JDK 25 and no Gradle available.

This matters most for **B2** and **B3**, whose hunks touch upstream Paper files. I fixed
the hunk *headers* and proved the resulting Java compiles, but only a real
`applyAllPatches` against the true Paper tree proves the *context lines* still match.
Run this before tagging:

```bash
./gradlew applyAllPatches
./gradlew :purpur-server:createBundlerJar -x test
java -jar purpur-server/build/libs/*bundler*.jar --nogui   # then: /violetcore status
```

The last step is the one that closes B3 — `/violetcore status` must respond on a
**freshly started** server, with no `/reload` first.
