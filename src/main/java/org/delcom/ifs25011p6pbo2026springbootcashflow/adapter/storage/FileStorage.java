package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/** Menyimpan upload ke UPLOAD_DIR dengan nama UUID + ekstensi. */
public class FileStorage {

    private final Path directory;

    public FileStorage(String uploadDir) {
        this.directory = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Mengembalikan nama file tersimpan (path relatif terhadap UPLOAD_DIR). */
    public String save(String originalName, byte[] data) throws DomainException {
        String name = UUID.randomUUID() + extensionOf(originalName);
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(name), data);
        } catch (IOException e) {
            throw new DomainException("Gagal menyimpan file: " + e.getMessage());
        }
        return name;
    }

    public Path directory() {
        return directory;
    }

    /** URL publik untuk file tersimpan (dilayani di /uploads/**). */
    public static String urlOf(String storedName) {
        return "/uploads/" + storedName;
    }

    static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        String ext = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,5}") ? "." + ext : "";
    }
}
