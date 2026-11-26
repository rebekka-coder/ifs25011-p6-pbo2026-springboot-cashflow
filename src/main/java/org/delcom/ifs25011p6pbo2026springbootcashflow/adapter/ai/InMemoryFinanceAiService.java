package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IFinanceAiService;

import java.util.List;

/** Stub AI deterministik untuk unit test (tanpa jaringan). */
public class InMemoryFinanceAiService implements IFinanceAiService {

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) {
        return List.of(
                new AiGeneratedTransaction("Pemasukan: " + prompt, 1_000_000L, TransactionType.INCOME),
                new AiGeneratedTransaction("Pengeluaran: " + prompt, 250_000L, TransactionType.EXPENSE));
    }
}
