package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageTest {

    @Test
    void savesFileWithUuidAndExtension(@TempDir Path dir) throws Exception {
        FileStorage storage = new FileStorage(dir.resolve("up").toString());
        String name = storage.save("Foto.PNG", new byte[]{1, 2, 3});
        assertThat(name).endsWith(".png");
        assertThat(Files.readAllBytes(storage.directory().resolve(name))).containsExactly(1, 2, 3);
        assertThat(FileStorage.urlOf(name)).isEqualTo("/uploads/" + name);
        assertThat(storage.save(null, new byte[0])).doesNotContain(".");
    }

    @Test
    void extensionRules() {
        assertThat(FileStorage.extensionOf(null)).isEmpty();
        assertThat(FileStorage.extensionOf("noext")).isEmpty();
        assertThat(FileStorage.extensionOf("trailing.")).isEmpty();
        assertThat(FileStorage.extensionOf("a.b-c")).isEmpty();
        assertThat(FileStorage.extensionOf("x.toolongext")).isEmpty();
        assertThat(FileStorage.extensionOf("x.JPeG")).isEqualTo(".jpeg");
    }

    @Test
    void failsWhenDirectoryCannotBeCreated(@TempDir Path dir) throws Exception {
        Path blocker = Files.writeString(dir.resolve("blocker"), "bukan folder");
        FileStorage storage = new FileStorage(blocker.toString());
        assertThrows(DomainException.class, () -> storage.save("a.png", new byte[]{1}));
    }
}
