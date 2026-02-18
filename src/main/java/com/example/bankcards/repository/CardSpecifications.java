package com.example.bankcards.repository;

import com.example.bankcards.entity.CreditCard;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CardSpecifications {
    private CardSpecifications() {}

    public static Specification<CreditCard> cardHolderLike(String cardholder) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("cardHolder")),
                        "%" + cardholder.toLowerCase() + "%");
    }

    public static Specification<CreditCard> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<CreditCard> hasStatus(CreditCard.CardStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<CreditCard> hasToBlock(Boolean toBlock) {
        return (root, query, cb) ->
                cb.equal(root.get("toBlock"), toBlock);
    }

    public static Specification<CreditCard> hasDate(LocalDate expiryDate) {
        return (root, query, cb) ->
                cb.equal(root.get("expiryDate"), expiryDate);
    }
}
