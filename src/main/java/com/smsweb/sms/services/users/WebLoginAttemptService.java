package com.smsweb.sms.services.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles failed WEB (form) logins. The mobile API has had this since
 * feature #10 (see services/mobile/LoginAttemptService) but the staff login
 * form had no throttling at all: a staff password could be guessed with
 * unlimited attempts, at full speed, leaving no trace beyond a log line.
 *
 * That matters more here than on mobile — a staff account reaches the whole
 * school's data and the fee ledger, and in practice school passwords are weak
 * and shared.
 *
 * Two independent counters:
 *   • per USERNAME — stops one account being hammered.
 *   • per IP       — stops "spraying": one common password tried against many
 *                    usernames, which never trips a per-username counter.
 *
 * In-memory, deliberately, matching the mobile service: a restart clears
 * lockouts, which is acceptable because the threat is an external guessing
 * script, and it avoids a schema migration. If you ever run more than one
 * instance, this state stops being shared and should move to a common store.
 */
@Component
public class WebLoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(WebLoginAttemptService.class);

    private static final int      MAX_PER_USER = 5;
    private static final int      MAX_PER_IP   = 20;   // higher: an office may share one NAT address
    private static final Duration WINDOW       = Duration.ofMinutes(15);
    private static final Duration LOCKOUT      = Duration.ofMinutes(15);

    private record Attempts(int count, Instant windowStart, Instant lockedUntil) {}

    private final Map<String, Attempts> byUser = new ConcurrentHashMap<>();
    private final Map<String, Attempts> byIp   = new ConcurrentHashMap<>();

    private static String key(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private boolean locked(Map<String, Attempts> m, String k) {
        Attempts a = m.get(k);
        return a != null && a.lockedUntil() != null && Instant.now().isBefore(a.lockedUntil());
    }

    private void record(Map<String, Attempts> m, String k, int max, String label) {
        Instant now = Instant.now();
        m.compute(k, (ignored, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart().plus(WINDOW))) {
                return new Attempts(1, now, null);
            }
            int next = existing.count() + 1;
            if (next >= max) {
                log.warn("Web login lockout triggered for {} '{}' after {} failed attempts", label, k, next);
                return new Attempts(next, existing.windowStart(), now.plus(LOCKOUT));
            }
            return new Attempts(next, existing.windowStart(), existing.lockedUntil());
        });
    }

    /** True if either the username or the caller's IP is currently locked out. */
    public boolean isBlocked(String username, String ip) {
        return locked(byUser, key(username)) || locked(byIp, key(ip));
    }

    /** Minutes left on the longest applicable lockout (rounded up), 0 if none. */
    public long remainingMinutes(String username, String ip) {
        long a = remaining(byUser, key(username));
        long b = remaining(byIp,   key(ip));
        return Math.max(a, b);
    }

    private long remaining(Map<String, Attempts> m, String k) {
        Attempts a = m.get(k);
        if (a == null || a.lockedUntil() == null) return 0;
        Duration d = Duration.between(Instant.now(), a.lockedUntil());
        return d.isNegative() ? 0 : d.toMinutes() + 1;
    }

    /** Record a failed attempt against both the username and the IP. */
    public void recordFailure(String username, String ip) {
        record(byUser, key(username), MAX_PER_USER, "user");
        record(byIp,   key(ip),       MAX_PER_IP,   "ip");
    }

    /** Successful login clears that username's counter (the IP counter decays on its own). */
    public void recordSuccess(String username) {
        byUser.remove(key(username));
    }
}
