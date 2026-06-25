package com.gradge.erp.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final int LOCK_TIME_DURATION = 15; // in minutes

    private final ConcurrentMap<String, FailedAttempt> attemptsCache = new ConcurrentHashMap<>();

    private static class FailedAttempt {
        int attempts;
        LocalDateTime lastAttempt;

        FailedAttempt() {
            this.attempts = 1;
            this.lastAttempt = LocalDateTime.now();
        }

        void increment() {
            this.attempts++;
            this.lastAttempt = LocalDateTime.now();
        }

        boolean isExpired(int lockDurationMinutes) {
            return lastAttempt.plusMinutes(lockDurationMinutes).isBefore(LocalDateTime.now());
        }
    }

    public void loginSucceeded(final String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(final String key) {
        attemptsCache.compute(key, (k, v) -> {
            if (v == null || v.isExpired(LOCK_TIME_DURATION)) {
                return new FailedAttempt();
            }
            v.increment();
            if (v.attempts >= MAX_ATTEMPT) {
                log.warn("Account/IP {} has been locked due to exceeding {} failed login attempts.", key, MAX_ATTEMPT);
            }
            return v;
        });
    }

    public boolean isBlocked(final String key) {
        FailedAttempt attempt = attemptsCache.get(key);
        if (attempt == null) {
            return false;
        }
        if (attempt.isExpired(LOCK_TIME_DURATION)) {
            attemptsCache.remove(key);
            return false;
        }
        return attempt.attempts >= MAX_ATTEMPT;
    }
}
