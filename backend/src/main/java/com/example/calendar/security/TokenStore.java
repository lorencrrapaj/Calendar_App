// src/main/java/com/example/calendar/security/TokenStore.java
package com.example.calendar.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    // token → email
    private final Map<String,String> store = new ConcurrentHashMap<>();

    /** Create and remember a fresh token for this email */
    public String create(String email) {
        String token = UUID.randomUUID().toString();
        store.put(token, email);
        return token;
    }

    /** Look up which email corresponds to this token (or null) */
    public String getEmail(String token) {
        if (token == null) {
            return null;
        }
        return store.get(token);
    }

    /** Invalidate a token on logout */
    public void invalidate(String token) {
        if (token == null) {
            return;
        }
        store.remove(token);
    }
}
