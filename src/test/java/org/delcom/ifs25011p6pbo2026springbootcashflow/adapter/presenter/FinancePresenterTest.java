package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction tx = new Transaction(3, 1, "Gaji", 1_000_000L, TransactionType.INCOME, null,
            LocalDateTime.of(2026, 3, 5, 9, 7));

    @Test
    void formatsMoneyAndLines() {
        assertThat(presenter.money(1_000_000L)).isEqualTo("Rp 1,000,000.00");
        assertThat(presenter.balanceLine(4_975_000L)).isEqualTo("Saldo: Rp 4,975,000.00");
        assertThat(presenter.typeLabel(TransactionType.EXPENSE)).isEqualTo("Pengeluaran");
        assertThat(presenter.line(tx)).isEqualTo("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]");
        assertThat(presenter.dateTime(tx.createdAt())).isEqualTo("05 Mar 2026 09:07");
    }

    @Test
    void formatsMessages() {
        assertThat(presenter.addedMessage(tx)).startsWith("Transaksi ditambahkan: [3]");
        assertThat(presenter.deletedMessage(tx)).startsWith("Transaksi dihapus: [3]");
        assertThat(presenter.aiSavedMessage(2)).isEqualTo("2 transaksi hasil AI ditambahkan");
    }
}
