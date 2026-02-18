package com.example.bankcards.dto;

import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.util.CardUtil;
import com.example.bankcards.util.CryptoUtils;
import com.example.bankcards.util.Mask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardRespDto {
    private Long id;
    private String cardHolder;
    @Mask
    private String cardNumber;
    private String expiryDate;
    private CreditCard.CardStatus status;
    private Boolean toBlock;
    private BigDecimal balance;
    private Long userId;

    public static CardRespDto toDto(CreditCard card, CryptoUtils cryptoUtils) {
        CardRespDto dto = new CardRespDto();
        dto.id = card.getId();
        dto.cardHolder = card.getCardHolder();
        dto.cardNumber = cryptoUtils.decrypt(card.getCardNumber());
        dto.expiryDate = CardUtil.formatExpiryDate(card.getExpiryDate());
        dto.status = card.getStatus();
        dto.toBlock = card.getToBlock();
        dto.balance = CardUtil.fromCentsToDecimal(card.getBalance());
        dto.userId = card.getUserId();

        return dto;
    }

    @Override
    public String toString() {
        return "CardResp{" +
                "id=" + id +
                ", cardHolder='" + cardHolder + '\'' +
                ", cardNumber='" + CardUtil.mask(cardNumber) + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", status=" + status +
                ", toBlock=" + toBlock +
                ", balance=" + balance +
                ", userId=" + userId +
                '}';
    }
}
