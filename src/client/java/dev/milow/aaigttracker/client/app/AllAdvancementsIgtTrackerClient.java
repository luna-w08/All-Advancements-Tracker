package dev.milow.aaigttracker.client.app;

import com.mojang.blaze3d.platform.InputConstants;
import dev.milow.aaigttracker.AllAdvancementsIgtTracker;
import dev.milow.aaigttracker.client.data.TrackerDataRepository;
import dev.milow.aaigttracker.client.manager.AdvancementTrackerManager;
import dev.milow.aaigttracker.client.model.CompletionVisibilityMode;
import dev.milow.aaigttracker.client.model.MenuBackgroundColor;
import dev.milow.aaigttracker.client.model.TrackerUiState;
import dev.milow.aaigttracker.client.model.WorldKey;
import dev.milow.aaigttracker.client.screen.AdvancementTrackerScreen;
import dev.milow.aaigttracker.client.waypoint.WaypointService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class AllAdvancementsIgtTrackerClient implements ClientModInitializer {
    private static AdvancementTrackerManager trackerManager;
    private static KeyMapping openTrackerKey;
    private static final Map<WorldKey, TrackerUiState> trackerUiStates = new HashMap<>();
    private static final WaypointService waypointService = new WaypointService();
    private static MenuBackgroundColor menuBackgroundColor = MenuBackgroundColor.CLASSIC;
    private static UUID observedPlayerId;
    private static boolean deathWaypointRecorded;

    @Override
    public void onInitializeClient() {
        TrackerDataRepository.registerReloadListener();
        trackerManager = new AdvancementTrackerManager();
        KeyMapping.Category trackerCategory = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(AllAdvancementsIgtTracker.MOD_ID, "tracker")
        );

        openTrackerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.aaigttracker.open_screen",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RSHIFT,
                trackerCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            trackerManager.tick(client);
            maybeRecordDeathWaypoint(client);

            while (openTrackerKey.consumeClick()) {
                if (client.player == null) {
                    continue;
                }

                if (client.gui.screen() instanceof AdvancementTrackerScreen) {
                    client.setScreenAndShow(null);
                } else {
                    client.setScreenAndShow(new AdvancementTrackerScreen(trackerManager));
                }
            }

        });

        AllAdvancementsIgtTracker.LOGGER.info("Registered client hooks for {}", AllAdvancementsIgtTracker.MOD_ID);
    }

    public static AdvancementTrackerManager getTrackerManager() {
        return trackerManager;
    }

    public static CompletionVisibilityMode getCompletionVisibilityMode(WorldKey worldKey) {
        return getTrackerUiState(worldKey).completionVisibilityMode();
    }

    public static void setCompletionVisibilityMode(WorldKey worldKey, CompletionVisibilityMode mode) {
        if (worldKey == null || mode == null) {
            return;
        }

        TrackerUiState currentState = getTrackerUiState(worldKey);
        trackerUiStates.put(
                worldKey,
                new TrackerUiState(
                        mode,
                        currentState.lastTrackerRootId(),
                        currentState.lastSelectedAdvancementId()
                )
        );
    }

    public static MenuBackgroundColor getMenuBackgroundColor() {
        return menuBackgroundColor;
    }

    public static void setMenuBackgroundColor(MenuBackgroundColor color) {
        if (color != null) {
            menuBackgroundColor = color;
        }
    }

    public static KeyMapping getOpenTrackerKey() {
        return openTrackerKey;
    }

    public static WaypointService getWaypointService() {
        return waypointService;
    }

    public static void rememberTrackerState(
            WorldKey worldKey,
            CompletionVisibilityMode completionVisibilityMode,
            Identifier rootId,
            Identifier selectedAdvancementId
    ) {
        if (worldKey == null) {
            return;
        }

        trackerUiStates.put(
                worldKey,
                new TrackerUiState(completionVisibilityMode, rootId, selectedAdvancementId)
        );
    }

    public static TrackerUiState getTrackerUiState(WorldKey worldKey) {
        if (worldKey == null) {
            return TrackerUiState.defaults();
        }

        return trackerUiStates.computeIfAbsent(worldKey, ignored -> TrackerUiState.defaults());
    }

    private static void maybeRecordDeathWaypoint(Minecraft client) {
        if (client == null || client.player == null) {
            observedPlayerId = null;
            deathWaypointRecorded = false;
            return;
        }

        UUID playerId = client.player.getUUID();
        if (!playerId.equals(observedPlayerId)) {
            observedPlayerId = playerId;
            deathWaypointRecorded = false;
        }

        boolean dead = client.player.isDeadOrDying() || client.player.getHealth() <= 0.0F;
        if (!dead) {
            deathWaypointRecorded = false;
            return;
        }

        if (deathWaypointRecorded || trackerManager == null || trackerManager.getCurrentWorldKey() == null) {
            return;
        }

        waypointService.createAtPlayer(trackerManager.getCurrentWorldKey(), client.player, "Death");
        deathWaypointRecorded = true;
    }
}
