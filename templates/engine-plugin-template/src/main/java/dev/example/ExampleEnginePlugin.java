package dev.example;

import io.violetmc.violetcore.engine.api.EnginePlugin;
import io.violetmc.violetcore.engine.api.EnginePluginContext;
import io.violetmc.violetcore.engine.api.TickObserver;

public final class ExampleEnginePlugin implements EnginePlugin {
    @Override
    public void onLoad(final EnginePluginContext context) {
        context.logger().info("Loaded " + context.description().displayName());

        context.registerTickObserver(new TickObserver() {
            @Override
            public void onServerTickEnd(final int tick, final double tickDurationMillis, final long remainingNanos) {
                if (tick % 100 == 0) {
                    context.logger().info("tick=" + tick + ", mspt=" + tickDurationMillis);
                }
            }
        });
    }
}
