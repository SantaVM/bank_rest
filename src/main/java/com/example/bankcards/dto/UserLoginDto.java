package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Аутентификация пользователя")
public class UserLoginDto {
    @NotEmpty
    @Email
    @Schema(description = "email", example = "email@email.com")
    private String email;

    @NotBlank
    @Size(min=6, max=20)
    @Schema(description = "password", example = "12345678")
    private String password;
}