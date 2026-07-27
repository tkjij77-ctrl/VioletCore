# SmartEntityTick

SmartEntityTick is the first official optional performance Engine Plugin for VioletCore.

## Goal

Reduce unnecessary ticking for safer entity categories when they are far away from players.

## Default behavior

By default it only throttles:

```text
ITEM
EXPERIENCE_ORB
ARMOR_STAND
```

It does **not** affect players. It does **not** affect animals or monsters by default.

## Install

Download from the release assets and place it in:

```text
engine-plugins/SmartEntityTick-1.0.0.jar
```

Start VioletCore:

```bash
java -Xmx4G -jar VioletCore-26.2-v0.7.0.jar --nogui --engine-plugins-dir engine-plugins
```

## Config

On first load, SmartEntityTick creates:

```text
engine-plugins/SmartEntityTick/smart-entity-tick.yml
```

Default config:

```yaml
enabled: true
min-distance-from-player: 48
item-tick-rate: 5
xp-orb-tick-rate: 10
armor-stand-tick-rate: 20
affect-animals: false
animal-tick-rate: 10
affect-monsters: false
monster-tick-rate: 5
```

## Safety notes

- Keep `affect-animals` and `affect-monsters` false unless you test your gameplay.
- Returning false from `EntityTickController` skips an entity tick for that tick.
- This plugin declares:

```yaml
modifies:
  - entity-ticking
```
