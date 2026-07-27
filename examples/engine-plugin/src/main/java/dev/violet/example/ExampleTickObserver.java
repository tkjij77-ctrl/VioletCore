package dev.violet.example;

import io.violetmc.violetcore.engine.api.EnginePlugin;
import io.violetmc.violetcore.engine.api.EnginePluginContext;
import io.violetmc.violetcore.engine.api.TickObserver;

public final class ExampleTickObserver implements EnginePlugin {
    @Override
    public void onLoad(final EnginePluginContext context) {
        context.logger().info("ExampleTickObserver loaded as " + context.description().displayName());
        context.registerTickObserver(new TickObserver() {
            @Override
            public void onServerTickEnd(final int tick, final double tickDurationMillis, final long remainingNanos) {
                if (tick % 100 == 0) {
                    context.logger().info("tick=" + tick + ", mspt=" + String.format(java.util.Locale.ROOT, "%.3f", tickDurationMillis) + ", remainingNanos=" + remainingNanos);
                }
            }
        });

        // Example only: allow all entities to tick normally. Real optimization
        // Engine Plugins can return false for selected entities/ticks.
        context.registerEntityTickController((entity, worldName, tick) -> true);
    }

    @Override
    public void onServerStarted(final EnginePluginContext context) {
        context.logger().info("Server reached Done state. Loaded plugins: " + context.loadedEnginePlugins());
    }

    @Override
    public void onServerStopping(final EnginePluginContext context) {
        context.logger().info("Server is stopping.");
    }
}
