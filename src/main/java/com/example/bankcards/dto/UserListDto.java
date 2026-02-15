package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

public record UserListDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role
) {
    public static UserListDto toDto(User user) {
        return new UserListDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}