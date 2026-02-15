package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Частичное обновление пользователя")
public class UserUpdateDto {
    @Email
    @Schema(
            description = "email",
            example = "email@email.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
            description = "First Name to print it on card",
            example = "TOM",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp="[A-Za-z]+", message = "Cardholder name must contain only" +
            " Latin letters")
    @Size(min=2, max=20)
    private String firstName;

    @Schema(
            description = "First Name to print it on card",
            example = "SMITH",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Pattern(regexp="[A-Za-z]+", message = "Cardholder last name must contain" +
            " only Latin letters")
    @Size(min=2, max=20)
    private String lastName;

    @Schema(
            description = "User role",
            example = "USER/ADMIN",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private User.Role role;
}
