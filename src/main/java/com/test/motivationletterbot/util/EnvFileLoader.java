package com.test.motivationletterbot.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class EnvFileLoader {
    private EnvFileLoader() {
    }

    public static void load() {
        Path env = Paths.get(".env");
        if (!Files.exists(env)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(env, StandardCharsets.UTF_8);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx <= 0) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                // set as system property so Spring Environment can see it
                System.setProperty(key, value);
                // also map common env->spring property for convenience
                if ("OPENAI_API_KEY".equals(key)) {
                    System.setProperty("openai.api.key", value);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load .env: " + e.getMessage());
        }
    }
}

