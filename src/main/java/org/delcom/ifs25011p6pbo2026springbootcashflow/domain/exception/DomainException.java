package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception;

/** Checked exception dasar untuk semua kegagalan domain. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }
}
