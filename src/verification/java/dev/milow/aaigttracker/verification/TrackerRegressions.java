package dev.milow.aaigttracker.verification;

import dev.milow.aaigttracker.client.manager.AdvancementTrackerManager;
import dev.milow.aaigttracker.client.model.AdvancementSnapshot;
import dev.milow.aaigttracker.client.timer.CompletionTimeline;
import dev.milow.aaigttracker.client.timer.TimerReading;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class TrackerRegressions {
    private static final Instant START = Instant.parse("2026-11-01T00:00:00Z");

    public static void main(String[] args) throws Exception {
        verifyTimeline();
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        verifyCapturePublication();
        System.out.println("PASS: completion timing, paused clocks, rollback protection, revocation, and nonblocking capture recovery");
    }

    private static void verifyTimeline() {
        CompletionTimeline<String> timeline = new CompletionTimeline<>();
        timeline.sample(START, 120_000);
        timeline.sample(START.plusMillis(200), 120_200);
        equal(120_100L, timeline.completionTime("stone", START.plusMillis(100)), "live completion");

        // Ten minutes in a paused GUI must not be subtracted from a completed advancement.
        timeline.sample(START.plusSeconds(600), 120_200);
        equal(120_100L, timeline.completionTime("stone", START.plusMillis(100)), "cached completion after pause");
        equal(120_200L, timeline.completionTime("paused", START.plusSeconds(300)), "flat paused interval");
        timeline.sample(START.plusMillis(600_200), 120_400);
        equal(120_300L, timeline.completionTime("resume", START.plusMillis(600_100)), "completion after resume");

        timeline.sample(START.plusSeconds(601), 0);
        equal(120_100L, timeline.completionTime("stone", START.plusMillis(100)), "zero clock cannot erase known time");
        timeline.sample(START.plusSeconds(602), 120_600);
        equal(120_500L, timeline.completionTime("next", START.plusMillis(601_100)), "sampling resumes after stale clock");
        equal(null, timeline.completionTime("historic", START.minusSeconds(3600)), "historical time is unknown, not zero");
        equal(null, timeline.completionTime("future", START.plusSeconds(3600)), "future award not extrapolated");
        equal(null, timeline.completionTime("stone", null), "revocation removes old time");
        timeline.sample(START.plusSeconds(603), 121_600);
        equal(121_100L, timeline.completionTime("stone", START.plusMillis(602_500)), "re-award receives new time");
        equal(null, timeline.completionTime("stone", START.minusSeconds(1)), "changed award timestamp cannot reuse unrelated time");

        // Repeated paused samples and a backwards system clock must not corrupt existing records.
        timeline.sample(START.plusSeconds(604), 121_600);
        timeline.sample(START.minusSeconds(60), 121_700);
        equal(121_100L, timeline.completionTime("late-packet", START.plusMillis(602_500)), "delayed progress uses earlier samples");
        equal(120_300L, timeline.completionTime("resume", START.plusMillis(600_100)), "wall clock change preserves known time");
        timeline.sample(START.plusSeconds(605), 121_800);
        equal(121_700L, timeline.completionTime("after-clock-change", START.plusMillis(604_500)), "wall-clock recovery");
        for (int i = 606; i < 6000; i++) {
            timeline.sample(START.plusSeconds(i), 121_800L + (i - 605) * 1000L);
        }
        equal(null, timeline.completionTime("stone-old", START.plusMillis(100)), "evicted unrecorded time is unknown");
        equal(120_300L, timeline.completionTime("resume", START.plusMillis(600_100)), "recorded awards survive sample eviction");
        timeline.startSession();
        timeline.sample(START.plusSeconds(10_000), 0);
        timeline.sample(START.plusSeconds(10_001), 1000);
        equal(500L, timeline.completionTime("new-world", START.plusMillis(10_000_500)), "fresh world clock can restart");
        equal(120_300L, timeline.completionTime("resume", START.plusMillis(600_100)), "matching awards survive world reload");
    }

    private static void verifyCapturePublication() throws Exception {
        AdvancementTrackerManager manager = new AdvancementTrackerManager();
        Class<?> stateType = nested("WorldTrackerState");
        Constructor<?> stateConstructor = stateType.getDeclaredConstructor();
        stateConstructor.setAccessible(true);
        Object state = stateConstructor.newInstance();
        Identifier id = Identifier.withDefaultNamespace("story/mine_stone");
        Component title = Component.literal("Stone Age");
        AdvancementSnapshot snapshot = new AdvancementSnapshot(id, id, title, ItemStack.EMPTY, title, title,
                ItemStack.EMPTY, true, 1.0F, 0, 0, 1, 1, List.of(), List.of(), 0, 10_500L, null);
        @SuppressWarnings("unchecked") Map<Identifier, AdvancementSnapshot> snapshots =
                (Map<Identifier, AdvancementSnapshot>) field(stateType, "snapshots").get(state);
        snapshots.put(id, snapshot);
        Field timelineField = field(stateType, "timeline");
        @SuppressWarnings("unchecked") CompletionTimeline<String> timeline = (CompletionTimeline<String>) timelineField.get(state);
        timeline.sample(START, 10_000);
        timeline.sample(START.plusSeconds(1), 11_000);
        equal(10_500L, timeline.completionTime("award", START.plusMillis(500)), "seed retained award");
        Class<?> pendingType = nested("PendingCapture");
        Constructor<?> pendingConstructor = pendingType.getDeclaredConstructors()[0];
        pendingConstructor.setAccessible(true);
        CompletableFuture<Object> incomplete = new CompletableFuture<>();
        Field pendingField = field(AdvancementTrackerManager.class, "pendingCapture");
        pendingField.set(manager, pendingConstructor.newInstance(state, null, incomplete));
        Method publish = AdvancementTrackerManager.class.getDeclaredMethod("publishCompletedCapture");
        publish.setAccessible(true);
        // A stalled/paused server task must not block publication on the client.
        CompletableFuture.runAsync(() -> {
            try { publish.invoke(manager); } catch (Exception e) { throw new RuntimeException(e); }
        }).get(2, TimeUnit.SECONDS);
        if (incomplete.isDone() || pendingField.get(manager) == null) {
            throw new AssertionError("unfinished capture was discarded or consumed");
        }
        incomplete.completeExceptionally(new IllegalStateException("Simulated transient capture failure"));
        publish.invoke(manager);
        equal(10_500L, timeline.completionTime("award", START.plusMillis(500)), "capture failure preserves time");
        equal(snapshot, snapshots.get(id), "capture failure preserves snapshot");

        Class<?> resultType = nested("CaptureResult");
        Constructor<?> resultConstructor = resultType.getDeclaredConstructors()[0];
        resultConstructor.setAccessible(true);
        Object empty = resultConstructor.newInstance(TimerReading.unavailable(), new HashMap<>());
        pendingField.set(manager, pendingConstructor.newInstance(state, null, CompletableFuture.completedFuture(empty)));
        publish.invoke(manager);
        equal(snapshot, snapshots.get(id), "empty transitional capture preserves progress");
        Object result = resultConstructor.newInstance(new TimerReading(true, 11_000, "test"), new HashMap<>(Map.of(id, snapshot)));
        pendingField.set(manager, pendingConstructor.newInstance(state, null, CompletableFuture.completedFuture(result)));
        publish.invoke(manager);
        if (pendingField.get(manager) != null) throw new AssertionError("completed capture not published");
        equal(new TimerReading(true, 11_000, "test"), field(stateType, "lastTimer").get(state), "recovery publishes timer");
        equal(10_500L, timeline.completionTime("award", START.plusMillis(500)), "recovery preserves time");
        Object sorted = field(stateType, "sortedSnapshots").get(state);
        equal(List.of(snapshot), sorted, "recovery publishes complete snapshot");
        field(AdvancementTrackerManager.class, "currentWorldKey").set(manager, new dev.milow.aaigttracker.client.model.WorldKey("test"));
        @SuppressWarnings("unchecked") Map<Object, Object> worlds = (Map<Object, Object>) field(AdvancementTrackerManager.class, "worldStates").get(manager);
        worlds.put(manager.getCurrentWorldKey(), state);
        if (manager.getSnapshots() != manager.getSnapshots() || manager.getSnapshots() != sorted) {
            throw new AssertionError("unchanged reads must reuse the published list");
        }
    }

    private static Class<?> nested(String name) throws ClassNotFoundException {
        return Class.forName(AdvancementTrackerManager.class.getName() + "$" + name);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field result = type.getDeclaredField(name);
        result.setAccessible(true);
        return result;
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
