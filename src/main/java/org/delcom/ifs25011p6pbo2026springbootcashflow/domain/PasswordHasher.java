package org.delcom.ifs25011p6pbo2026springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash / verifikasi password dengan SHA-256 (hex). */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        byte[] digest = sha256().digest(raw.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    public static boolean verify(String raw, String hash) {
        return hash(raw).equals(hash);
    }

    @Generated
    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }
}
