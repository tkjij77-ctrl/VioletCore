package io.violetmc.smartentitytick;

import io.violetmc.violetcore.engine.api.EnginePlugin;
import io.violetmc.violetcore.engine.api.EnginePluginContext;
import io.violetmc.violetcore.engine.api.EngineStatsProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.LongAdder;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public final class SmartEntityTick implements EnginePlugin, EngineStatsProvider {
    private final LongAdder checks = new LongAdder();
    private final LongAdder skippedTotal = new LongAdder();
    private final LongAdder skippedItems = new LongAdder();
    private final LongAdder skippedXpOrbs = new LongAdder();
    private final LongAdder skippedArmorStands = new LongAdder();
    private final LongAdder skippedAnimals = new LongAdder();
    private final LongAdder skippedMonsters = new LongAdder();
    private Settings settings;

    private static final int CATEGORY_NONE = 0;
    private static final int CATEGORY_ITEM = 1;
    private static final int CATEGORY_XP_ORB = 2;
    private static final int CATEGORY_ARMOR_STAND = 3;
    private static final int CATEGORY_ANIMAL = 4;
    private static final int CATEGORY_MONSTER = 5;

    @Override
    public void onLoad(final EnginePluginContext context) throws Exception {
        this.settings = Settings.load(context.dataDirectory().resolve("smart-entity-tick.yml"));
        context.logger().info("SmartEntityTick loaded: " + this.settings);
        context.registerEntityTickController((entity, worldName, tick) -> shouldTick(entity, tick));
    }

    @Override
    public void onServerStarted(final EnginePluginContext context) {
        context.logger().info("SmartEntityTick active. This plugin only throttles safe entity categories by default.");
    }

    private boolean shouldTick(final Entity entity, final int tick) {
        if (!this.settings.enabled) {
            return true;
        }
        if (entity instanceof Player) {
            return true;
        }

        // Resolve the category and tick rate FIRST. The previous implementation ran an
        // O(players) distance scan for every entity on every tick, including the large
        // majority of entities that are never throttled. Non-candidates now exit in O(1).
        final int category = categoryOf(entity);
        if (category == CATEGORY_NONE) {
            return true;
        }

        final int rate = rateFor(category);
        if (rate <= 1) {
            return true;
        }

        // On a tick where this entity is scheduled to run anyway there is nothing to
        // decide, so the expensive distance scan is skipped entirely.
        if (tick % rate == 0) {
            return true;
        }

        if (entity.isDead() || !entity.isValid()) {
            return true;
        }
        if (!isFarFromPlayers(entity)) {
            return true;
        }

        this.checks.increment();
        return skip(counterFor(category));
    }

    /**
     * Classifies an entity using Bukkit's own type hierarchy.
     *
     * <p>The previous implementation matched substrings against {@code EntityType.name()},
     * which mis-bucketed many vanilla types: {@code ZOMBIFIED_PIGLIN} and {@code PIGLIN_BRUTE}
     * matched "PIG" and were treated as animals, {@code ZOMBIE_HORSE} and
     * {@code SKELETON_HORSE} matched both lists at once, and {@code MOOSHROOM},
     * {@code SNIFFER}, {@code ARMADILLO}, {@code ZOGLIN} and {@code ENDERMITE} matched
     * neither. Checking {@link Monster} before {@link Animals} also makes hybrid
     * undead-mount types resolve deterministically instead of depending on branch order.</p>
     */
    private int categoryOf(final Entity entity) {
        final EntityType type = entity.getType();
        if (type == EntityType.ITEM) {
            return CATEGORY_ITEM;
        }
        if (type == EntityType.EXPERIENCE_ORB) {
            return CATEGORY_XP_ORB;
        }
        if (type == EntityType.ARMOR_STAND) {
            return CATEGORY_ARMOR_STAND;
        }
        if (this.settings.affectMonsters && entity instanceof Monster) {
            return CATEGORY_MONSTER;
        }
        if (this.settings.affectAnimals && entity instanceof Animals) {
            return CATEGORY_ANIMAL;
        }
        return CATEGORY_NONE;
    }

    private int rateFor(final int category) {
        return switch (category) {
            case CATEGORY_ITEM -> this.settings.itemTickRate;
            case CATEGORY_XP_ORB -> this.settings.xpOrbTickRate;
            case CATEGORY_ARMOR_STAND -> this.settings.armorStandTickRate;
            case CATEGORY_ANIMAL -> this.settings.animalTickRate;
            case CATEGORY_MONSTER -> this.settings.monsterTickRate;
            default -> 1;
        };
    }

    private LongAdder counterFor(final int category) {
        return switch (category) {
            case CATEGORY_ITEM -> this.skippedItems;
            case CATEGORY_XP_ORB -> this.skippedXpOrbs;
            case CATEGORY_ARMOR_STAND -> this.skippedArmorStands;
            case CATEGORY_ANIMAL -> this.skippedAnimals;
            default -> this.skippedMonsters;
        };
    }

    private boolean skip(final LongAdder adder) {
        adder.increment();
        this.skippedTotal.increment();
        return false;
    }

    @Override
    public List<String> stats() {
        return List.of(
            "checks=" + this.checks.sum(),
            "skipped-total=" + this.skippedTotal.sum(),
            "skipped-items=" + this.skippedItems.sum(),
            "skipped-xp-orbs=" + this.skippedXpOrbs.sum(),
            "skipped-armor-stands=" + this.skippedArmorStands.sum(),
            "skipped-animals=" + this.skippedAnimals.sum(),
            "skipped-monsters=" + this.skippedMonsters.sum()
        );
    }

    @Override
    public void resetStats() {
        this.checks.reset();
        this.skippedTotal.reset();
        this.skippedItems.reset();
        this.skippedXpOrbs.reset();
        this.skippedArmorStands.reset();
        this.skippedAnimals.reset();
        this.skippedMonsters.reset();
    }

    private boolean isFarFromPlayers(final Entity entity) {
        final World world = entity.getWorld();
        // getPlayers() returns a fresh defensive copy in CraftBukkit and was previously
        // invoked twice per entity per tick. It is now resolved once.
        final List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            // Conservative: with nobody in the world, leave ticking untouched rather than
            // silently changing hopper/farm timings in unattended dimensions.
            return false;
        }

        final double minDistance = this.settings.minDistanceFromPlayer;
        final double minDistanceSquared = minDistance * minDistance;

        // Raw coordinate comparison avoids allocating a Location for the entity and for
        // every player on every check. Iterating this world's own player list also makes
        // the previous per-player world identity test redundant.
        final double ex = entity.getX();
        final double ey = entity.getY();
        final double ez = entity.getZ();

        for (int i = 0; i < players.size(); i++) {
            final Player player = players.get(i);
            if (!player.isOnline() || player.isDead()) {
                continue;
            }
            final double dx = player.getX() - ex;
            final double dy = player.getY() - ey;
            final double dz = player.getZ() - ez;
            if (dx * dx + dy * dy + dz * dz < minDistanceSquared) {
                return false;
            }
        }
        return true;
    }

    private record Settings(boolean enabled, int minDistanceFromPlayer, int itemTickRate, int xpOrbTickRate, int armorStandTickRate, boolean affectAnimals, int animalTickRate, boolean affectMonsters, int monsterTickRate) {
        static Settings defaults() { return new Settings(true, 48, 5, 10, 20, false, 10, false, 5); }
        static Settings load(final Path path) throws IOException {
            if (Files.notExists(path)) {
                final Path parent = path.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.writeString(path, "# SmartEntityTick configuration\n" + "enabled: true\n" + "min-distance-from-player: 48\n" + "item-tick-rate: 5\n" + "xp-orb-tick-rate: 10\n" + "armor-stand-tick-rate: 20\n" + "affect-animals: false\n" + "animal-tick-rate: 10\n" + "affect-monsters: false\n" + "monster-tick-rate: 5\n", StandardCharsets.UTF_8);
            }
            Settings d = defaults();
            boolean enabled = d.enabled; int minDistanceFromPlayer = d.minDistanceFromPlayer; int itemTickRate = d.itemTickRate; int xpOrbTickRate = d.xpOrbTickRate; int armorStandTickRate = d.armorStandTickRate; boolean affectAnimals = d.affectAnimals; int animalTickRate = d.animalTickRate; boolean affectMonsters = d.affectMonsters; int monsterTickRate = d.monsterTickRate;
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String raw;
                while ((raw = reader.readLine()) != null) {
                    String line = raw.split("#", 2)[0].trim();
                    if (line.isEmpty() || !line.contains(":")) continue;
                    String[] parts = line.split(":", 2); String key = parts[0].trim().toLowerCase(Locale.ROOT); String value = parts[1].trim();
                    switch (key) {
                        case "enabled" -> enabled = parseBoolean(value, enabled);
                        case "min-distance-from-player" -> minDistanceFromPlayer = parseInt(value, minDistanceFromPlayer, 8, 512);
                        case "item-tick-rate" -> itemTickRate = parseInt(value, itemTickRate, 1, 200);
                        case "xp-orb-tick-rate" -> xpOrbTickRate = parseInt(value, xpOrbTickRate, 1, 200);
                        case "armor-stand-tick-rate" -> armorStandTickRate = parseInt(value, armorStandTickRate, 1, 200);
                        case "affect-animals" -> affectAnimals = parseBoolean(value, affectAnimals);
                        case "animal-tick-rate" -> animalTickRate = parseInt(value, animalTickRate, 1, 200);
                        case "affect-monsters" -> affectMonsters = parseBoolean(value, affectMonsters);
                        case "monster-tick-rate" -> monsterTickRate = parseInt(value, monsterTickRate, 1, 200);
                        default -> { }
                    }
                }
            }
            return new Settings(enabled, minDistanceFromPlayer, itemTickRate, xpOrbTickRate, armorStandTickRate, affectAnimals, animalTickRate, affectMonsters, monsterTickRate);
        }
        private static int parseInt(final String value, final int fallback, final int min, final int max) { try { return Math.max(min, Math.min(max, Integer.parseInt(value))); } catch (Exception ignored) { return fallback; } }
        private static boolean parseBoolean(final String value, final boolean fallback) { return switch (value.toLowerCase(Locale.ROOT)) { case "true", "yes", "1", "on" -> true; case "false", "no", "0", "off" -> false; default -> fallback; }; }
    }
}
