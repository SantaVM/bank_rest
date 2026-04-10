package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserFilterDto(
        @Pattern(regexp="[A-Za-z]+", message = "Must contain only Latin letters")
        String firstName,

        @Pattern(regexp="[A-Za-z]+", message = "Must contain only Latin letters")
        String lastName,

        @Email
        String email,

        User.Role role) {
}
