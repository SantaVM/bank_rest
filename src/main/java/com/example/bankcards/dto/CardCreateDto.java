package com.example.bankcards.dto;

import com.example.bankcards.util.validators.TwoDecimalPlaces;
import com.example.bankcards.util.validators.ValidExpirationDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.math.BigDecimal;

@Getter
@Setter
public class CardCreateDto {
    @Positive
    @NotNull
    @Schema(type = "integer", example = "51")
    private Long userId;

    @CreditCardNumber
    @NotNull
    @Schema(example = "4000006806224829")
    private String cardNumber;

    @NotNull
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry date must be in MM/yy format")
    @ValidExpirationDate
    private String expiryDate;

    @NotNull
    @TwoDecimalPlaces
    @Schema(example = "1234.55")
    private BigDecimal balance;
}
