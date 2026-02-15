package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

public record UserFilterDto(String firstName, String lastName, String email,
                            User.Role role) {
}
