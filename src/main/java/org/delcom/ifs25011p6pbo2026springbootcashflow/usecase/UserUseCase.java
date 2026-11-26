package org.delcom.ifs25011p6pbo2026springbootcashflow.usecase;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

public class UserUseCase {

    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> listUsers() {
        return users.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /** newPassword kosong/null berarti password tidak diubah. */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = getUser(id);
        String name = Checks.text(fullName);
        Checks.require(!name.isEmpty(), "Nama lengkap wajib diisi");
        String hash = user.passwordHash();
        if (newPassword != null && !newPassword.isBlank()) {
            Checks.require(newPassword.length() >= 4, "Password minimal 4 karakter");
            hash = PasswordHasher.hash(newPassword);
        }
        return users.update(user.withProfile(name, hash));
    }

    public User updatePhoto(int id, String photoPath) throws DomainException {
        return users.update(getUser(id).withPhoto(photoPath));
    }
}
