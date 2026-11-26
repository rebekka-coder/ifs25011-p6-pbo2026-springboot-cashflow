package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
