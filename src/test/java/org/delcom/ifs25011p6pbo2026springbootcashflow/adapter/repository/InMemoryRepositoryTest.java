package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;

class InMemoryRepositoryTest extends RepositoryContractTest {

    @Override
    IUserRepository newUsers() {
        return new UserRepository();
    }

    @Override
    ITransactionRepository newTxs() {
        return new TransactionRepository();
    }
}
