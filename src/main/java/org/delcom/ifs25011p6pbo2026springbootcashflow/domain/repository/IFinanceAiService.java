package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;

import java.util.List;

public interface IFinanceAiService {

    List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException;
}
