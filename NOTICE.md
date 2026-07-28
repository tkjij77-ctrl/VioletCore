# NOTICE

VioletCore is a fork of [PurpurMC/Purpur](https://github.com/PurpurMC/Purpur), which is
itself a fork of [PaperMC/Paper](https://github.com/PaperMC/Paper). It is a server-software
prototype and is **not** an independent codebase: the overwhelming majority of this
repository is upstream code.

## What is original to VioletCore

Only the Engine Plugin layer:

- `purpur-api/paper-patches/features/0003-VioletCore-Engine-Plugin-API.patch`
- `purpur-server/paper-patches/features/0006-VioletCore-engine-plugin-loader.patch`
- `purpur-server/minecraft-patches/features/0022-VioletCore-engine-plugin-hooks.patch`
- `examples/`, `templates/`, `docs/`, and the VioletCore-specific CI workflows

These files are offered under the MIT License, © 2026 VioletCore contributors.

## What is upstream

Everything else, including all other patches under `purpur-*/`, the entire
`purpur-api/src` and `purpur-server/src` trees, `build-data/`, and `scripts/`.

| Project | Role | License |
|---|---|---|
| [PurpurMC/Purpur](https://github.com/PurpurMC/Purpur) | Direct upstream; source in `purpur-api/src`, `purpur-server/src` | MIT, © 2019-2024 PurpurMC |
| [PaperMC/Paper](https://github.com/PaperMC/Paper) | Upstream of Purpur | GPL-3.0 (portions MIT) |
| Spigot / Bukkit / CraftBukkit | Upstream of Paper | GPL-3.0 |
| [Paperweight](https://github.com/PaperMC/paperweight) | Build tooling | MIT |

## Licensing of the distributed jar

The root `LICENSE` file covers this repository's own contents and retains the
PurpurMC copyright notice as the MIT License requires.

**The built server jar is a different matter.** It embeds Paper, Spigot, Bukkit and
CraftBukkit code, which is licensed under the **GNU General Public License version 3**.
Paper's own `LICENSE.md` states that it inherits GPL-3.0 from Spigot, which inherits it
from Bukkit and CraftBukkit.

Anyone redistributing a compiled VioletCore server jar is therefore distributing a
GPL-3.0 combined work and must comply with GPL-3.0, including making corresponding
source available. Redistributing this repository's own patches and documentation alone
is covered by MIT.

This mirrors how Paper and Purpur handle the same situation: a permissive licence on the
fork's own contributions, with GPL-3.0 obligations attaching to the assembled server.

## Trademarks

Minecraft, Mojang, and Microsoft trademarks belong to their respective owners. This
project is not affiliated with, endorsed by, or sponsored by Mojang Studios, Microsoft,
PaperMC, or PurpurMC.
