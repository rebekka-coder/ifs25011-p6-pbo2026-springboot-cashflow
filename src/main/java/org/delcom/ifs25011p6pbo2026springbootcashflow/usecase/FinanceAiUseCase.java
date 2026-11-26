package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IFinanceAiService;

import java.util.List;

public class FinanceAiUseCase {

    private final IFinanceAiService ai;
    private final FinanceUseCase finance;

    public FinanceAiUseCase(IFinanceAiService ai, FinanceUseCase finance) {
        this.ai = ai;
        this.finance = finance;
    }

    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        Checks.require(!Checks.text(prompt).isEmpty(), "Prompt tidak boleh kosong");
        return ai.generateTransactions(Checks.text(prompt));
    }

    public int saveGeneratedTransactions(int userId, List<AiGeneratedTransaction> items) throws DomainException {
        for (AiGeneratedTransaction item : items) {
            finance.addTransaction(userId, item.description(), item.amount(), item.type(), null);
        }
        return items.size();
    }
}
