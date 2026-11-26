package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {

    @Override
    protected int idOf(Transaction entity) {
        return entity.id();
    }

    @Override
    protected String entityName() {
        return "Transaksi";
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        Transaction transaction = new Transaction(nextId(), userId, description, amount, type, proofPath,
                LocalDateTime.now());
        put(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return snapshot().stream().filter(t -> t.userId() == userId).toList();
    }
}
