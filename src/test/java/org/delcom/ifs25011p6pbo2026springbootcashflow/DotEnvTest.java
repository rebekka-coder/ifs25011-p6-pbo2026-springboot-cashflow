package org.delcom.ifs25011p6pbo2026springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotEnvTest {

    private static final List<String> KEYS = List.of("CF_T_PLAIN", "CF_T_DQ", "CF_T_SQ", "CF_T_EMPTY",
            "CF_T_MIX1", "CF_T_MIX2", "CF_T_EXIST", "PATH");

    @AfterEach
    void cleanup() {
        KEYS.forEach(System::clearProperty);
    }

    @Test
    void loadsValuesAndSkipsInvalidLines(@TempDir Path dir) throws Exception {
        System.setProperty("CF_T_EXIST", "keep");
        Path env = dir.resolve(".env");
        Files.writeString(env, String.join("\n",
                "# komentar", "", "tanpa-sama-dengan", "=tanpa-kunci",
                "CF_T_PLAIN= plain value ", "CF_T_DQ=\"quoted value\"", "CF_T_SQ='single'", "CF_T_EMPTY=",
                "CF_T_MIX1=\"abc'", "CF_T_MIX2='abc\"", "CF_T_EXIST=override", "PATH=zzz"));

        DotEnv.load(env);

        assertThat(System.getProperty("CF_T_PLAIN")).isEqualTo("plain value");
        assertThat(System.getProperty("CF_T_DQ")).isEqualTo("quoted value");
        assertThat(System.getProperty("CF_T_SQ")).isEqualTo("single");
        assertThat(System.getProperty("CF_T_EMPTY")).isEmpty();
        assertThat(System.getProperty("CF_T_MIX1")).isEqualTo("\"abc'");
        assertThat(System.getProperty("CF_T_MIX2")).isEqualTo("'abc\"");
        assertThat(System.getProperty("CF_T_EXIST")).isEqualTo("keep");
        assertThat(System.getProperty("PATH")).isNull();
    }

    @Test
    void missingFileIsIgnored(@TempDir Path dir) {
        DotEnv.load(dir.resolve("tidak-ada.env"));
        DotEnv.load();
    }

    @Test
    void unreadableFileThrows(@TempDir Path dir) {
        assertThrows(UncheckedIOException.class, () -> DotEnv.load(dir));
    }
}
