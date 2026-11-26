package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.ValidationException;

final class Checks {

    private Checks() {
    }

    static String text(String value) {
        return value == null ? "" : value.trim();
    }

    static void require(boolean condition, String message) throws ValidationException {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}
