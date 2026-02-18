package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        description = "DTO для регистрации нового пользователя"
)
public class UserRegisterDto {
    @NotEmpty
    @Email
    @Schema(description = "email", example = "email@email.com")
    private String email;

    @NotBlank
    @Size(min=6, max=20)
    @Schema(description = "password", example = "12345678")
    private String password;

    @Schema(description = "First Name to print it on card", example = "TOM")
    @NotBlank
    @Pattern(regexp="[A-Za-z]+", message = "Cardholder name must contain only" +
            " Latin letters")
    @Size(min=2, max=20)
    private String firstName;

    @Schema(description = "First Name to print it on card", example = "SMITH")
    @NotBlank
    @Pattern(regexp="[A-Za-z]+", message = "Cardholder last name must contain" +
            " only Latin letters")
    @Size(min=2, max=20)
    private String lastName;
}
