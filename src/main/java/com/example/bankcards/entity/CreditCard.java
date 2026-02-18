package com.example.bankcards.entity;

import com.example.bankcards.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String cardHolder;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "credit_card_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean toBlock = false;

    @Column(nullable = false)
    private BigInteger balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    public void changeStatus(CardStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessException("Cannot change status from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED;

        public boolean canTransitionTo(CardStatus newStatus) {
            return switch (this) {
                case ACTIVE -> newStatus == BLOCKED || newStatus == EXPIRED;
                case BLOCKED -> newStatus == ACTIVE || newStatus == EXPIRED;
                case EXPIRED -> false;
            };
        }
    }
}
