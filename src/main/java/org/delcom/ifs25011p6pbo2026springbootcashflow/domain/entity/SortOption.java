package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {
    NEWEST_FIRST("Terbaru", Comparator.comparing(Transaction::createdAt).thenComparing(Transaction::id).reversed()),
    OLDEST_FIRST("Terlama", Comparator.comparing(Transaction::createdAt).thenComparing(Transaction::id)),
    AMOUNT_HIGH("Jumlah terbesar", Comparator.comparingLong(Transaction::amount).reversed()),
    AMOUNT_LOW("Jumlah terkecil", Comparator.comparingLong(Transaction::amount)),
    DESCRIPTION_AZ("Keterangan A-Z", Comparator.comparing(Transaction::description, String.CASE_INSENSITIVE_ORDER)),
    TYPE("Tipe", Comparator.comparing(Transaction::type).thenComparing(Transaction::id));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
