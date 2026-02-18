package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> firstNameLike(String firstName) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%");
    }

    public static Specification<User> lastNameLike(String lastName) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%");
    }

    public static Specification<User> emailLike(String email) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> hasRole(User.Role role) {
        return (root, query, cb) ->
                cb.equal(root.get("role"), role);
    }
}

