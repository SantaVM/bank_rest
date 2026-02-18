package com.example.bankcards.dto;

import com.example.bankcards.entity.CreditCard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardHolderListDto {
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
