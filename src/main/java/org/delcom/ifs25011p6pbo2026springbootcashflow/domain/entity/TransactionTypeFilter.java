package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

public enum TransactionTypeFilter {
    ALL("Semua", null),
    INCOME("Pemasukan", TransactionType.INCOME),
    EXPENSE("Pengeluaran", TransactionType.EXPENSE);

    private final String label;
    private final TransactionType type;

    TransactionTypeFilter(String label, TransactionType type) {
        this.label = label;
        this.type = type;
    }

    public String label() {
        return label;
    }

    public boolean matches(Transaction transaction) {
        return type == null || transaction.type() == type;
    }
}
