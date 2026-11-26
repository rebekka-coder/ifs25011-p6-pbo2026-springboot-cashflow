package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

public record Balance(int count, long income, long expense) {

    public long balance() {
        return income - expense;
    }
}
