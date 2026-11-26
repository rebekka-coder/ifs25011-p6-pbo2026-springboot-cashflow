package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

public class FinanceUseCase {

    private final ITransactionRepository transactions;

    public FinanceUseCase(ITransactionRepository transactions) {
        this.transactions = transactions;
    }

    public static long parseAmount(String text) throws ValidationException {
        long value;
        try {
            value = Long.parseLong(Checks.text(text));
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        Checks.require(value > 0, "Jumlah harus lebih dari 0");
        return value;
    }

    public Transaction addTransaction(int userId, String description, String amountText,
                                      TransactionType type, String proofPath) throws DomainException {
        return addTransaction(userId, description, parseAmount(amountText), type, proofPath);
    }

    public Transaction addTransaction(int userId, String description, long amount,
                                      TransactionType type, String proofPath) throws DomainException {
        String desc = Checks.text(description);
        Checks.require(userId > 0, "ID pengguna tidak valid");
        Checks.require(!desc.isEmpty(), "Keterangan wajib diisi");
        Checks.require(amount > 0, "Jumlah harus lebih dari 0");
        Checks.require(type != null, "Tipe transaksi wajib dipilih");
        return transactions.save(userId, desc, amount, type, proofPath);
    }

    public List<Transaction> listTransactions(int userId, String keyword,
                                              TransactionTypeFilter filter, SortOption sort) {
        String key = Checks.text(keyword).toLowerCase(Locale.ROOT);
        return transactions.findByUserId(userId).stream()
                .filter(filter::matches)
                .filter(t -> key.isEmpty() || t.description().toLowerCase(Locale.ROOT).contains(key))
                .sorted(sort.comparator())
                .toList();
    }

    public void deleteTransaction(int requesterId, int transactionId) throws DomainException {
        Checks.require(transactionId > 0, "ID transaksi tidak valid");
        Transaction transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", transactionId));
        Checks.require(transaction.userId() == requesterId, "Transaksi bukan milik Anda");
        transactions.deleteById(transactionId);
    }

    public Balance getBalance(int userId) {
        List<Transaction> all = transactions.findByUserId(userId);
        long income = sum(all, TransactionType.INCOME);
        long expense = sum(all, TransactionType.EXPENSE);
        return new Balance(all.size(), income, expense);
    }

    private static long sum(List<Transaction> all, TransactionType type) {
        return all.stream().filter(t -> t.type() == type).mapToLong(Transaction::amount).sum();
    }
}
