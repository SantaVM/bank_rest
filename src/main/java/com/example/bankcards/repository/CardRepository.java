package com.example.bankcards.repository;

import com.example.bankcards.entity.CreditCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CreditCard, Long> {

    boolean existsByCardNumber(String cardNumber);

    @Query("""
      SELECT c FROM CreditCard c WHERE
      c.userId = COALESCE(:userId, c.userId) AND
      c.cardHolder LIKE COALESCE(:cardHolder, c.cardHolder) AND
      c.status = COALESCE(:status, c.status) AND
      c.toBlock = COALESCE(:toBlock, c.toBlock) AND
      c.expiryDate = COALESCE(:expiryDate, c.expiryDate)
      """
    )
    Page<CreditCard> findByCriteria(
            @Param("userId") Long userId,
            @Param("cardHolder") String cardHolder,
            @Param("status") CreditCard.CardStatus status,
            @Param("toBlock") Boolean toBlock,
            @Param("expiryDate") LocalDate expiryDate,
            Pageable pageable
    );

    // можно использовать вместо SELECT FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditCard c WHERE c.id = :id")
    Optional<CreditCard> findByIdWithLock(@Param("id") Long id);

    @Query(value = "SELECT * FROM credit_card WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<CreditCard> findByIdForUpdate(@Param("id") Long id);
}
