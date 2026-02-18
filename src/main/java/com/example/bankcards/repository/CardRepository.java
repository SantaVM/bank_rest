package com.example.bankcards.repository;

import com.example.bankcards.entity.CreditCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CreditCard, Long>, JpaSpecificationExecutor<CreditCard> {

    // можно использовать вместо SELECT FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditCard c WHERE c.id = :id")
    Optional<CreditCard> findByIdWithLock(@Param("id") Long id);

    @Query(value = "SELECT * FROM credit_card WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<CreditCard> findByIdForUpdate(@Param("id") Long id);

    /**
     * Атомарное списание средств.
     * Деньги спишутся ТОЛЬКО если balance >= amount.
     *
     * @return количество обновлённых строк (0 или 1)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE CreditCard c
            SET c.balance = c.balance - :amount
        WHERE c.id = :cardId
            AND c.userId = :userId
            AND c.balance >= :amount
            AND c.status = :status
            AND c.toBlock = :toBlock
        """)
    int withdraw(
            @Param("cardId") Long cardId,
            @Param("userId") Long userId,
            @Param("amount") BigInteger amount,
            @Param("status") CreditCard.CardStatus status,
            @Param("toBlock") Boolean toBlock
    );

    /**
     * Атомарное зачисление средств.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE CreditCard c
           SET c.balance = c.balance + :amount
         WHERE c.id = :cardId
            AND c.userId = :userId
            AND c.status = :status
            AND c.toBlock = :toBlock
    """)
    int deposit(
            @Param("cardId") Long cardId,
            @Param("userId") Long userId,
            @Param("amount") BigInteger amount,
            @Param("status") CreditCard.CardStatus status,
            @Param("toBlock") Boolean toBlock
    );

    @Query("""
        select coalesce(sum(c.balance), 0)
        from CreditCard c
        where c.userId = :userId
    """)
    BigInteger sumBalanceByUserId(@Param("userId") Long userId);
}
