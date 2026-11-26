package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity;

public record User(int id, String username, String passwordHash, String fullName, Role role, String photoPath) {

    public User withProfile(String newFullName, String newPasswordHash) {
        return new User(id, username, newPasswordHash, newFullName, role, photoPath);
    }

    public User withPhoto(String newPhotoPath) {
        return new User(id, username, passwordHash, fullName, role, newPhotoPath);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
