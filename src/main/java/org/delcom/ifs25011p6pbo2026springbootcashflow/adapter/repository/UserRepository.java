package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.User;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected int idOf(User entity) {
        return entity.id();
    }

    @Override
    protected String entityName() {
        return "Pengguna";
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId(), username, passwordHash, fullName, role, null);
        put(user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return snapshot().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
    }
}
