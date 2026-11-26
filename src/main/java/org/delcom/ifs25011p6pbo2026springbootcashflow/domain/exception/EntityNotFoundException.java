package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entity, int id) {
        super(entity + " dengan ID " + id + " tidak ditemukan");
    }
}
