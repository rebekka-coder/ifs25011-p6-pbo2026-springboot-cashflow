package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Kontrak yang sama dijalankan untuk implementasi InMemory dan JDBC (H2). */
abstract class RepositoryContractTest {

    protected IUserRepository users;
    protected ITransactionRepository txs;

    abstract IUserRepository newUsers();

    abstract ITransactionRepository newTxs();

    @BeforeEach
    void init() {
        users = newUsers();
        txs = newTxs();
    }

    @Test
    void userCrud() throws Exception {
        User alice = users.save("alice", "hash", "Alice", Role.USER);
        assertThat(alice.id()).isPositive();
        assertThat(users.findById(alice.id())).contains(alice);
        assertThat(users.findById(999)).isEmpty();
        assertThat(users.findByUsername("ALICE")).isPresent();
        assertThat(users.findByUsername("bob")).isEmpty();
        users.save("bob", "h", "Bob", Role.ADMIN);
        assertThat(users.findAll()).hasSize(2);

        User changed = alice.withProfile("Alice B", "h2").withPhoto("p.png");
        assertThat(users.update(changed)).isEqualTo(changed);
        assertThat(users.findById(alice.id()).orElseThrow().photoPath()).isEqualTo("p.png");
        assertThrows(EntityNotFoundException.class,
                () -> users.update(new User(999, "x", "h", "X", Role.USER, null)));

        users.deleteById(alice.id());
        assertThat(users.findById(alice.id())).isEmpty();
        assertThrows(EntityNotFoundException.class, () -> users.deleteById(alice.id()));
    }

    @Test
    void transactionCrud() throws Exception {
        User owner = users.save("carol", "h", "Carol", Role.USER);
        User other = users.save("dave", "h", "Dave", Role.USER);
        Transaction salary = txs.save(owner.id(), "Gaji", 5_000_000L, TransactionType.INCOME, null);
        txs.save(owner.id(), "Makan", 25_000L, TransactionType.EXPENSE, "bukti.png");
        txs.save(other.id(), "Lain", 1L, TransactionType.INCOME, null);

        assertThat(salary.createdAt()).isNotNull();
        assertThat(txs.findById(salary.id())).isPresent();
        assertThat(txs.findById(999)).isEmpty();
        assertThat(txs.findByUserId(owner.id())).hasSize(2);
        assertThat(txs.findAll()).hasSize(3);

        Transaction changed = new Transaction(salary.id(), owner.id(), "Gaji Baru", 6_000_000L,
                TransactionType.INCOME, "x.png", salary.createdAt());
        assertThat(txs.update(changed).description()).isEqualTo("Gaji Baru");
        Transaction reloaded = txs.findById(salary.id()).orElseThrow();
        assertThat(reloaded.amount()).isEqualTo(6_000_000L);
        assertThat(reloaded.proofPath()).isEqualTo("x.png");
        assertThrows(EntityNotFoundException.class, () -> txs.update(
                new Transaction(999, owner.id(), "x", 1, TransactionType.INCOME, null, salary.createdAt())));

        txs.deleteById(salary.id());
        assertThat(txs.findById(salary.id())).isEmpty();
        assertThrows(EntityNotFoundException.class, () -> txs.deleteById(salary.id()));
    }
}
