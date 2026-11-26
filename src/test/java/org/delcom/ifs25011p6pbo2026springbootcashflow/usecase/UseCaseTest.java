package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UseCaseTest {

    private IUserRepository users;
    private TransactionRepository txRepo;
    private AuthUseCase auth;
    private UserUseCase userUseCase;
    private FinanceUseCase finance;
    private FinanceAiUseCase financeAi;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        txRepo = new TransactionRepository();
        auth = new AuthUseCase(users);
        userUseCase = new UserUseCase(users);
        finance = new FinanceUseCase(txRepo);
        financeAi = new FinanceAiUseCase(new InMemoryFinanceAiService(), finance);
    }

    // ---------------- Auth ----------------
    @Test
    void registerAndLogin() throws Exception {
        User user = auth.register("  Alice ", "secret", " Alice A ");
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.fullName()).isEqualTo("Alice A");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(PasswordHasher.verify("secret", user.passwordHash())).isTrue();
        assertThat(auth.login("ALICE", "secret").id()).isEqualTo(user.id());
    }

    @Test
    void registerValidation() throws Exception {
        auth.register("alice", "secret", "Alice");
        assertThrows(ValidationException.class, () -> auth.register("ab", "secret", "X"));
        assertThrows(ValidationException.class, () -> auth.register(null, "secret", "X"));
        assertThrows(ValidationException.class, () -> auth.register("bob", "secret", "  "));
        assertThrows(ValidationException.class, () -> auth.register("bob", null, "Bob"));
        assertThrows(ValidationException.class, () -> auth.register("bob", "abc", "Bob"));
        assertThrows(ValidationException.class, () -> auth.register("ALICE", "secret", "Dup"));
    }

    @Test
    void loginFailures() throws Exception {
        auth.register("alice", "secret", "Alice");
        assertThrows(ValidationException.class, () -> auth.login("nobody", "secret"));
        assertThrows(ValidationException.class, () -> auth.login("alice", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("alice", null));
    }

    @Test
    void ensureRootAdmin() throws Exception {
        auth.ensureRootAdmin("admin", "admin");
        auth.ensureRootAdmin("admin", "admin");
        assertThat(users.findAll()).hasSize(1);
        assertThat(users.findAll().get(0).role()).isEqualTo(Role.ADMIN);
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("admin", null));
    }

    // ---------------- User ----------------
    @Test
    void userUseCase() throws Exception {
        User alice = auth.register("alice", "secret", "Alice");
        assertThat(userUseCase.listUsers()).hasSize(1);
        assertThat(userUseCase.getUser(alice.id()).username()).isEqualTo("alice");
        assertThrows(EntityNotFoundException.class, () -> userUseCase.getUser(999));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updateProfile(999, "X", null));

        String oldHash = userUseCase.updateProfile(alice.id(), "Alice B", null).passwordHash();
        assertThat(oldHash).isEqualTo(alice.passwordHash());
        assertThat(userUseCase.updateProfile(alice.id(), "Alice B", "  ").passwordHash()).isEqualTo(oldHash);
        User changed = userUseCase.updateProfile(alice.id(), " Alice C ", "newpass");
        assertThat(changed.fullName()).isEqualTo("Alice C");
        assertThat(PasswordHasher.verify("newpass", changed.passwordHash())).isTrue();
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(alice.id(), " ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(alice.id(), "A", "abc"));

        assertThat(userUseCase.updatePhoto(alice.id(), "f.png").photoPath()).isEqualTo("f.png");
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updatePhoto(999, "f.png"));
    }

    // ---------------- Finance ----------------
    @Test
    void parseAmount() throws Exception {
        assertThat(FinanceUseCase.parseAmount("1000")).isEqualTo(1000);
        assertThat(FinanceUseCase.parseAmount(" 50 ")).isEqualTo(50);
        assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("abc"));
        assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount(null));
        assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("0"));
        assertThrows(ValidationException.class, () -> FinanceUseCase.parseAmount("-5"));
    }

    @Test
    void addTransactionValidation() throws Exception {
        Transaction t = finance.addTransaction(1, " Gaji ", "5000000", TransactionType.INCOME, null);
        assertThat(t.description()).isEqualTo("Gaji");
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, " ", 10L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(0, "x", 10L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", 0L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", 10L, null, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", "abc", TransactionType.INCOME, null));
    }

    @Test
    void listAndBalance() throws Exception {
        finance.addTransaction(1, "Gaji", 5_000_000L, TransactionType.INCOME, null);
        finance.addTransaction(1, "Makan siang", 25_000L, TransactionType.EXPENSE, null);
        finance.addTransaction(1, "Kopi", 15_000L, TransactionType.EXPENSE, null);
        finance.addTransaction(2, "Orang lain", 1L, TransactionType.INCOME, null);

        List<String> newest = finance.listTransactions(1, null, TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST)
                .stream().map(Transaction::description).toList();
        assertThat(newest).containsExactly("Kopi", "Makan siang", "Gaji");
        assertThat(finance.listTransactions(1, "MAKAN", TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST)).hasSize(1);
        assertThat(finance.listTransactions(1, "", TransactionTypeFilter.EXPENSE, SortOption.AMOUNT_HIGH)).hasSize(2);
        assertThat(finance.listTransactions(1, "gaji", TransactionTypeFilter.INCOME, SortOption.TYPE)).hasSize(1);
        assertThat(finance.listTransactions(1, "zzz", TransactionTypeFilter.ALL, SortOption.TYPE)).isEmpty();

        Balance balance = finance.getBalance(1);
        assertThat(balance.count()).isEqualTo(3);
        assertThat(balance.income()).isEqualTo(5_000_000L);
        assertThat(balance.expense()).isEqualTo(40_000L);
        assertThat(balance.balance()).isEqualTo(4_960_000L);
        assertThat(finance.getBalance(99).balance()).isZero();
    }

    @Test
    void deleteTransaction() throws Exception {
        Transaction mine = finance.addTransaction(1, "Gaji", 10L, TransactionType.INCOME, null);
        assertThrows(ValidationException.class, () -> finance.deleteTransaction(1, 0));
        assertThrows(EntityNotFoundException.class, () -> finance.deleteTransaction(1, 999));
        assertThrows(ValidationException.class, () -> finance.deleteTransaction(2, mine.id()));
        finance.deleteTransaction(1, mine.id());
        assertThat(finance.getBalance(1).count()).isZero();
    }

    // ---------------- Finance AI ----------------
    @Test
    void financeAi() throws Exception {
        List<AiGeneratedTransaction> items = financeAi.generateTransactions("  makan ");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).description()).isEqualTo("Pemasukan: makan");
        assertThrows(ValidationException.class, () -> financeAi.generateTransactions("  "));
        assertThrows(ValidationException.class, () -> financeAi.generateTransactions(null));

        assertThat(financeAi.saveGeneratedTransactions(1, items)).isEqualTo(2);
        assertThat(finance.getBalance(1).count()).isEqualTo(2);
        assertThat(finance.getBalance(1).balance()).isEqualTo(750_000L);
    }
}
