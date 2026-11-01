package dev.milow.aaigttracker.client.timer;

import net.minecraft.client.server.IntegratedServer;

public final class TimerSource {
    /** Read on the integrated server thread, using one clock across all player dimensions. */
    public TimerReading read(IntegratedServer server) {
        if (server == null) {
            return TimerReading.multiplayerDisabled();
        }
        if (!server.isSameThread()) {
            throw new IllegalStateException("Tracker game time must be sampled on the server thread");
        }
        return new TimerReading(true, Math.max(0L, server.overworld().getGameTime()) * 50L,
                "screen.aaigttracker.timer.level");
    }
}
