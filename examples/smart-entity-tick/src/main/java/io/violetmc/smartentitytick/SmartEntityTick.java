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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
        if (!this.settings.enabled) return true;
        if (entity instanceof Player) return true;
        if (entity.isDead() || !entity.isValid()) return true;
        if (!isFarFromPlayers(entity)) return true;

        this.checks.increment();
        final EntityType type = entity.getType();
        if (type == EntityType.ITEM && this.settings.itemTickRate > 1 && tick % this.settings.itemTickRate != 0) return skip(this.skippedItems);
        if (type == EntityType.EXPERIENCE_ORB && this.settings.xpOrbTickRate > 1 && tick % this.settings.xpOrbTickRate != 0) return skip(this.skippedXpOrbs);
        if (type == EntityType.ARMOR_STAND && this.settings.armorStandTickRate > 1 && tick % this.settings.armorStandTickRate != 0) return skip(this.skippedArmorStands);
        if (this.settings.affectAnimals && isAnimal(type) && this.settings.animalTickRate > 1 && tick % this.settings.animalTickRate != 0) return skip(this.skippedAnimals);
        if (this.settings.affectMonsters && isMonster(type) && this.settings.monsterTickRate > 1 && tick % this.settings.monsterTickRate != 0) return skip(this.skippedMonsters);
        return true;
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
        final double minDistanceSquared = this.settings.minDistanceFromPlayer * this.settings.minDistanceFromPlayer;
        for (final Player player : entity.getWorld().getPlayers()) {
            if (!player.isOnline() || player.isDead()) continue;
            if (player.getWorld() != entity.getWorld()) continue;
            if (player.getLocation().distanceSquared(entity.getLocation()) < minDistanceSquared) return false;
        }
        return !entity.getWorld().getPlayers().isEmpty();
    }

    private static boolean isAnimal(final EntityType type) {
        final String name = type.name();
        return name.contains("COW") || name.contains("PIG") || name.contains("SHEEP") || name.contains("CHICKEN") || name.contains("RABBIT") || name.contains("HORSE") || name.contains("DONKEY") || name.contains("LLAMA") || name.contains("GOAT") || name.contains("CAMEL");
    }

    private static boolean isMonster(final EntityType type) {
        final String name = type.name();
        return name.contains("ZOMBIE") || name.contains("SKELETON") || name.contains("CREEPER") || name.contains("SPIDER") || name.contains("ENDERMAN") || name.contains("DROWNED") || name.contains("HUSK") || name.contains("STRAY") || name.contains("WITCH") || name.contains("SLIME");
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
