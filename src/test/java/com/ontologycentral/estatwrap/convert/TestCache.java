package com.ontologycentral.estatwrap.convert;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Smoke test for the caffeine cache (replaced the JSR107 RI, which nothing in
 * production used either — this documents the intended caching setup).
 *
 * @author aharth
 */
public class TestCache extends TestCase {
    Logger _log = Logger.getLogger(this.getClass().getName());

    public void testCache() throws Exception {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(1))
                .recordStats()
                .build();

        cache.put("foo", "bar");
        String result = cache.getIfPresent("foo");
        assertEquals("bar", result);
        _log.log(Level.INFO, "{0}", result);
    }
}
