package com.campus.api.http;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiPayloads {

    private ApiPayloads() {
        // Utility class.
    }

    public static Map<String, Object> error(int status, String code, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", status);
        payload.put("code", code);
        payload.put("message", message);
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    public static Map<String, Object> created(String message, String location, Object data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        payload.put("location", location);
        payload.put("data", data);
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }
}