package com.jch.backendapi.global.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class InMemoryRateLimiter {

    private final Map<String, RateLimitWindow> windows = new HashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter() {
        this(Clock.systemUTC());
    }

    InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized boolean tryAcquire(String key, int capacity, Duration windowDuration) {
        long nowMillis = clock.millis();
        cleanExpiredWindows(nowMillis);

        RateLimitWindow window = windows.get(key);
        if (window == null || window.isExpired(nowMillis)) {
            windows.put(key, RateLimitWindow.start(nowMillis, windowDuration));
            return true;
        }

        if (window.count() >= capacity) {
            return false;
        }

        window.increase();
        return true;
    }

    private void cleanExpiredWindows(long nowMillis) {
        Iterator<RateLimitWindow> iterator = windows.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired(nowMillis)) {
                iterator.remove();
            }
        }
    }

    private static class RateLimitWindow {

        private final long expiresAtMillis;
        private int count;

        private RateLimitWindow(long expiresAtMillis, int count) {
            this.expiresAtMillis = expiresAtMillis;
            this.count = count;
        }

        static RateLimitWindow start(long nowMillis, Duration windowDuration) {
            return new RateLimitWindow(nowMillis + windowDuration.toMillis(), 1);
        }

        boolean isExpired(long nowMillis) {
            return nowMillis >= expiresAtMillis;
        }

        int count() {
            return count;
        }

        void increase() {
            count += 1;
        }
    }
}
