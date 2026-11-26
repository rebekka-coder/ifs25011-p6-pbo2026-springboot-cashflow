package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang / teks / pesan. Bukan tanggung jawab entity. */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    public String money(long amount) {
        return String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount));
    }

    public String typeLabel(TransactionType type) {
        return type.label();
    }

    public String dateTime(LocalDateTime value) {
        return DATE_TIME.format(value);
    }

    /** Contoh: [1] Gaji | Rp 1,000,000.00 | [Pemasukan] */
    public String line(Transaction t) {
        return "[" + t.id() + "] " + t.description() + " | " + money(t.amount()) + " | [" + typeLabel(t.type()) + "]";
    }

    public String balanceLine(long balance) {
        return "Saldo: " + money(balance);
    }

    public String addedMessage(Transaction t) {
        return "Transaksi ditambahkan: " + line(t);
    }

    public String deletedMessage(Transaction t) {
        return "Transaksi dihapus: " + line(t);
    }

    public String aiSavedMessage(int count) {
        return count + " transaksi hasil AI ditambahkan";
    }
}
