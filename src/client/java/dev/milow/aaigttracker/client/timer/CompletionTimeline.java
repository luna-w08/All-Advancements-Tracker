package dev.milow.aaigttracker.client.timer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/** Maps newly observed awards onto sampled game time without extrapolating across pauses. */
public final class CompletionTimeline<K> {
    private final Map<K, Award> awards = new HashMap<>();
    private static final int MAX_SAMPLES = 4096;
    private final NavigableMap<Instant, Long> samples = new TreeMap<>();

    public void startSession() {
        // Keep times for matching award timestamps, but do not interpolate across a world reload.
        this.samples.clear();
    }

    public void sample(Instant instant, long millis) {
        if (instant == null || millis < 0L) {
            return;
        }
        // A stale clock packet or dimension transition must never erase recorded awards.
        Map.Entry<Instant, Long> latest = this.samples.lastEntry();
        if (latest != null && (millis < latest.getValue() || !instant.isAfter(latest.getKey()))) {
            return;
        }
        this.samples.put(instant, millis);
        if (this.samples.size() > MAX_SAMPLES) {
            this.samples.pollFirstEntry();
        }
    }

    public Long completionTime(K key, Instant obtained) {
        if (obtained == null) {
            this.awards.remove(key);
            return null;
        }
        Award cached = this.awards.get(key);
        if (cached != null && cached.obtained.equals(obtained)) {
            return cached.millis;
        }
        this.awards.remove(key);
        // Pre-existing progress has no trustworthy time anchor. Display unknown, never a fabricated zero.
        Map.Entry<Instant, Long> before = this.samples.floorEntry(obtained);
        Map.Entry<Instant, Long> after = this.samples.ceilingEntry(obtained);
        if (before == null || after == null) {
            return null;
        }
        long wallMillis = Duration.between(before.getKey(), after.getKey()).toMillis();
        double fraction = wallMillis == 0 ? 1.0
                : Duration.between(before.getKey(), obtained).toMillis() / (double) wallMillis;
        long millis = before.getValue() + Math.round(Math.clamp(fraction, 0.0, 1.0)
                * (after.getValue() - before.getValue()));
        this.awards.put(key, new Award(obtained, millis));
        return millis;
    }

    private record Award(Instant obtained, long millis) {}
}
