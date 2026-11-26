package org.delcom.ifs25011p6pbo2026springbootcashflow.domain;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);

    private static Transaction tx(int id, String desc, long amount, TransactionType type, int day) {
        return new Transaction(id, 7, desc, amount, type, "p.png", T0.plusDays(day));
    }

    private static List<Integer> sorted(SortOption option) {
        List<Transaction> all = List.of(tx(1, "b", 100, TransactionType.INCOME, 0),
                tx(2, "A", 300, TransactionType.EXPENSE, 1), tx(3, "c", 200, TransactionType.INCOME, 2));
        return all.stream().sorted(option.comparator()).map(Transaction::id).toList();
    }

    @Test
    void transactionAndBalance() {
        Transaction income = tx(1, "Gaji", 500, TransactionType.INCOME, 0);
        Transaction expense = tx(2, "Makan", 200, TransactionType.EXPENSE, 0);
        assertThat(income.signedAmount()).isEqualTo(500);
        assertThat(expense.signedAmount()).isEqualTo(-200);
        assertThat(income.id()).isEqualTo(1);
        assertThat(income.userId()).isEqualTo(7);
        assertThat(income.description()).isEqualTo("Gaji");
        assertThat(income.amount()).isEqualTo(500);
        assertThat(income.type()).isEqualTo(TransactionType.INCOME);
        assertThat(income.proofPath()).isEqualTo("p.png");
        assertThat(income.createdAt()).isEqualTo(T0);

        Balance balance = new Balance(2, 500, 200);
        assertThat(balance.balance()).isEqualTo(300);
        assertThat(balance.count()).isEqualTo(2);
        assertThat(balance.income()).isEqualTo(500);
        assertThat(balance.expense()).isEqualTo(200);

        AiGeneratedTransaction ai = new AiGeneratedTransaction("x", 5, TransactionType.EXPENSE);
        assertThat(ai.description()).isEqualTo("x");
        assertThat(ai.amount()).isEqualTo(5);
        assertThat(ai.type().label()).isEqualTo("Pengeluaran");
        assertThat(TransactionType.INCOME.label()).isEqualTo("Pemasukan");
    }

    @Test
    void userCopies() {
        User user = new User(1, "alice", "h", "Alice", Role.USER, null);
        User edited = user.withProfile("Alice B", "h2").withPhoto("f.png");
        assertThat(edited.id()).isEqualTo(1);
        assertThat(edited.username()).isEqualTo("alice");
        assertThat(edited.passwordHash()).isEqualTo("h2");
        assertThat(edited.fullName()).isEqualTo("Alice B");
        assertThat(edited.role()).isEqualTo(Role.USER);
        assertThat(edited.photoPath()).isEqualTo("f.png");
        assertThat(user.isAdmin()).isFalse();
        assertThat(new User(2, "root", "h", "Root", Role.ADMIN, null).isAdmin()).isTrue();
    }

    @Test
    void sortOptions() {
        assertThat(sorted(SortOption.NEWEST_FIRST)).containsExactly(3, 2, 1);
        assertThat(sorted(SortOption.OLDEST_FIRST)).containsExactly(1, 2, 3);
        assertThat(sorted(SortOption.AMOUNT_HIGH)).containsExactly(2, 3, 1);
        assertThat(sorted(SortOption.AMOUNT_LOW)).containsExactly(1, 3, 2);
        assertThat(sorted(SortOption.DESCRIPTION_AZ)).containsExactly(2, 1, 3);
        assertThat(sorted(SortOption.TYPE)).containsExactly(1, 3, 2);
        for (SortOption option : SortOption.values()) {
            assertThat(option.label()).isNotBlank();
        }
    }

    @Test
    void typeFilter() {
        Transaction income = tx(1, "a", 1, TransactionType.INCOME, 0);
        Transaction expense = tx(2, "b", 1, TransactionType.EXPENSE, 0);
        assertThat(TransactionTypeFilter.ALL.matches(income)).isTrue();
        assertThat(TransactionTypeFilter.ALL.matches(expense)).isTrue();
        assertThat(TransactionTypeFilter.INCOME.matches(income)).isTrue();
        assertThat(TransactionTypeFilter.INCOME.matches(expense)).isFalse();
        assertThat(TransactionTypeFilter.EXPENSE.matches(expense)).isTrue();
        assertThat(TransactionTypeFilter.EXPENSE.matches(income)).isFalse();
        for (TransactionTypeFilter filter : TransactionTypeFilter.values()) {
            assertThat(filter.label()).isNotBlank();
        }
    }

    @Test
    void passwordHasher() {
        assertThat(PasswordHasher.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(PasswordHasher.verify("abc", PasswordHasher.hash("abc"))).isTrue();
        assertThat(PasswordHasher.verify("abd", PasswordHasher.hash("abc"))).isFalse();
    }

    @Test
    void exceptions() {
        assertThat(new EntityNotFoundException("Pengguna", 5)).hasMessage("Pengguna dengan ID 5 tidak ditemukan")
                .isInstanceOf(DomainException.class);
        assertThat(new ValidationException("salah")).hasMessage("salah").isInstanceOf(DomainException.class);
    }
}
