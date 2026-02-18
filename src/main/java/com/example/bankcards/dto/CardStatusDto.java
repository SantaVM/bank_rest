package com.example.bankcards.dto;

import com.example.bankcards.entity.CreditCard;

public record CardStatusDto(CreditCard.CardStatus newStatus) {
}
