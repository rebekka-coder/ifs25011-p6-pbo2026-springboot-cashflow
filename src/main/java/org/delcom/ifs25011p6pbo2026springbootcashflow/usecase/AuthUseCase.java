package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.Locale;

public class AuthUseCase {

    private static final String BAD_CREDENTIALS = "Username atau password salah";

    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        String name = normalize(username);
        String full = Checks.text(fullName);
        Checks.require(name.length() >= 3, "Username minimal 3 karakter");
        Checks.require(!full.isEmpty(), "Nama lengkap wajib diisi");
        Checks.require(password != null && password.length() >= 4, "Password minimal 4 karakter");
        Checks.require(users.findByUsername(name).isEmpty(), "Username sudah dipakai");
        return users.save(name, PasswordHasher.hash(password), full, Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        User user = users.findByUsername(normalize(username))
                .orElseThrow(() -> new ValidationException(BAD_CREDENTIALS));
        Checks.require(password != null && PasswordHasher.verify(password, user.passwordHash()), BAD_CREDENTIALS);
        return user;
    }

    /** Seed admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public void ensureRootAdmin(String username, String password) throws DomainException {
        String name = normalize(username);
        Checks.require(!name.isEmpty() && !Checks.text(password).isEmpty(), "ROOT_USERNAME / ROOT_PASSWORD wajib diisi");
        if (users.findByUsername(name).isEmpty()) {
            users.save(name, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }

    private static String normalize(String username) {
        return Checks.text(username).toLowerCase(Locale.ROOT);
    }
}
