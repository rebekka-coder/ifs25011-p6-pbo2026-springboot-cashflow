package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public record Transaction(int id, int userId, String description, long amount,
                          TransactionType type, String proofPath, LocalDateTime createdAt) {

    /** Pemasukan bernilai positif, pengeluaran negatif. */
    public long signedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }
}
