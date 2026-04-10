package com.example.bankcards.dto;

import com.example.bankcards.entity.CreditCard;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardsListDto {
    @Min(1)
    private Long userId;

    @Pattern(regexp="[A-Za-z]+", message = "Cardholder string must contain only Latin letters")
    private String cardHolder;

    private CreditCard.CardStatus status;

    private Boolean toBlock;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$")
    private String expiryDate;
}
