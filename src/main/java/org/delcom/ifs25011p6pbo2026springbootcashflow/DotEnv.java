package org.delcom.ifs25011p6pbo2026springbootcashflow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Memuat file .env ke system properties sebelum Spring Boot start. */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    static void load(Path file) {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                apply(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void apply(String raw) {
        String line = raw.trim();
        int eq = line.indexOf('=');
        if (line.startsWith("#") || eq <= 0) {
            return;
        }
        String key = line.substring(0, eq).trim();
        String value = line.substring(eq + 1).trim();
        boolean quoted = value.length() >= 2
                && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"));
        if (quoted) {
            value = value.substring(1, value.length() - 1);
        }
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
