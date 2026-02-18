package com.example.bankcards.dto;

import com.example.bankcards.entity.CreditCard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardsListDto {
    @Schema(
            description = "Cardholder User ID",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "10"
    )
    @Min(1)
    private Long userId;

    @Schema(
            description = "Cardholder name to compare in LIKE statement",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "TOM"
    )
    @Pattern(regexp="[A-Za-z]+", message = "Cardholder string must contain " +
            "only Latin letters")
    private String cardHolder;

    @Schema(
            description = "Card status",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "ACTIVE/BLOCKED/EXPIRED"
    )
    private CreditCard.CardStatus status;

    @Schema(
            description = "User's request to block the Card",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "false"
    )
    private Boolean toBlock;

    @Schema(
            description = "Card expiry date MM/yy",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "12/25"
    )
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$")
    private String expiryDate;
}
