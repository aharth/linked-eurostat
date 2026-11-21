package com.ontologycentral.estatwrap;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What this wrapper has seen of its upstreams, per kind, recorded by
 * {@link HttpClientUtil#respond} on every call and read by {@code /status}, so the status never
 * calls Eurostat itself. Kinds, from the URL ({@link #kindOf}):
 * <ul>
 * <li>{@code data}: SDMX data ({@code /da/}),</li>
 * <li>{@code structure}: SDMX structure ({@code /cl/ /cs/ /ds/ /df/ /dc/}),</li>
 * <li>{@code catalogue}: every other Eurostat call (TOC, RSS feed, codelist inventory),</li>
 * <li>{@code self}: the SPARQL endpoint fetching its {@code FROM} graphs back through the public
 * base (reverse proxy, rate limiter).</li>
 * </ul>
 * Kept apart because they fail apart (linked-regionalstatistik, 2026-11-21: every data call
 * timed out for hours while the catalogue answered in under a second).
 */
public final class UpstreamHealth {

    /**
     * The last outcomes of one kind.
     *
     * @param lastSuccess when a call last succeeded, or {@code null}
     * @param lastFailure when a call last failed, or {@code null}
     * @param lastError the last failure's message
     * @param lastFailedUrl the URL that last failed
     * @param failuresInRow failures since the last success
     */
    public record Kind(Instant lastSuccess, Instant lastFailure, String lastError,
            String lastFailedUrl, int failuresInRow) {

        /** {@code ok}, {@code failing} (the latest call failed) or {@code unknown} (no call yet). */
        public String state() {
            if (lastFailure != null && (lastSuccess == null || lastFailure.isAfter(lastSuccess))) {
                return "failing";
            }
            return lastSuccess == null ? "unknown" : "ok";
        }
    }

    public static final String DATA = "data";
    public static final String STRUCTURE = "structure";
    public static final String CATALOGUE = "catalogue";
    public static final String SELF = "self";

    private static final Map<String, Kind> KINDS = new ConcurrentHashMap<>();

    private UpstreamHealth() {
    }

    /** The kind a URL belongs to. */
    public static String kindOf(String url) {
        if (url == null || !url.startsWith("https://ec.europa.eu/eurostat/")) {
            return SELF;
        }
        if (url.contains("/sdmx/") && url.contains("/data/")) {
            return DATA;
        }
        return url.contains("/sdmx/") ? STRUCTURE : CATALOGUE;
    }

    /** Record a call to {@code url} that upstream answered. */
    public static void success(String url) {
        KINDS.compute(kindOf(url), (k, v) -> new Kind(Instant.now(),
                v == null ? null : v.lastFailure(), v == null ? null : v.lastError(),
                v == null ? null : v.lastFailedUrl(), 0));
    }

    /** Record a call to {@code url} that failed with {@code e}. */
    public static void failure(String url, Exception e) {
        String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
        KINDS.compute(kindOf(url), (k, v) -> new Kind(v == null ? null : v.lastSuccess(),
                Instant.now(), msg.length() > 500 ? msg.substring(0, 500) + "…" : msg, url,
                v == null ? 1 : v.failuresInRow() + 1));
    }

    /** The outcomes of {@code kind}, never null. */
    public static Kind get(String kind) {
        return KINDS.getOrDefault(kind, new Kind(null, null, null, null, 0));
    }

    /** For tests. */
    public static void reset() {
        KINDS.clear();
    }
}
