package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

public record UserRespDto(
        Long id,
        String email,
        User.Role role,
        String firstName,
        String lastName
) {
    public static  UserRespDto toDto(User user) {
        return new UserRespDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName());
    }
}
