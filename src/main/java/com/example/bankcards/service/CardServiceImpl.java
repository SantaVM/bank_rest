package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.OperationRejectedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardSpecifications;
import com.example.bankcards.util.CardUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CardServiceImpl implements CardService{

    private final CardRepository repository;
    private final UserService userService;

    @Override
    public Page<CardRespDto> getUserCards(CardHolderListDto filter,
                                          Authentication auth, Pageable pageable) {
        Long userId = extractUserId(auth);

        Specification<CreditCard> spec = Specification.unrestricted();

        spec = spec.and(CardSpecifications.hasUserId(userId));

        if (filter.getExpiryDate() != null) {
            LocalDate date = CardUtil.parseExpiryDate(filter.getExpiryDate());
            spec = spec.and(
                    CardSpecifications.hasDate(date)
            );
        }

        if (filter.getStatus()!=null) {
            spec = spec.and(CardSpecifications.hasStatus(filter.getStatus()));
        }

        if (filter.getToBlock()!=null) {
            spec = spec.and(CardSpecifications.hasToBlock(filter.getToBlock()));
        }

        return repository.findAll(spec, pageable).map(CardRespDto::toDto);

    }

    @Override
    public Page<CardRespDto> getCardsList(CardsListDto filter, Pageable pageable) {

        Specification<CreditCard> spec = Specification.unrestricted();

        if (filter.getUserId() != null) {
            spec = spec.and(CardSpecifications.hasUserId(filter.getUserId()));
        }

        if (filter.getCardHolder() != null && !filter.getCardHolder().isBlank()) {
            spec = spec.and(
                    CardSpecifications.cardHolderLike(filter.getCardHolder())
            );
        }

        if (filter.getExpiryDate() != null) {
            LocalDate date = CardUtil.parseExpiryDate(filter.getExpiryDate());
            spec = spec.and(
                    CardSpecifications.hasDate(date)
            );
        }

        if (filter.getStatus()!=null) {
            spec = spec.and(CardSpecifications.hasStatus(filter.getStatus()));
        }

        if (filter.getToBlock()!=null) {
            spec = spec.and(CardSpecifications.hasToBlock(filter.getToBlock()));
        }

        return repository.findAll(spec, pageable).map(CardRespDto::toDto);
    }

    @Override
    public String generate() {
        return CardUtil.generateCardNumber();
    }

    @Transactional
    @Override
    public  CardRespDto create(CardCreateDto dto) {
        User user = userService.findOne(dto.getUserId());

        CreditCard newCard = CreditCard.builder()
                .owner(user)
                .cardHolder(user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase())
                .cardNumber(dto.getCardNumber())
                .expiryDate(CardUtil.parseExpiryDate(dto.getExpiryDate()))
                .balance(CardUtil.getAmountAsBigInteger(dto.getBalance()))
                .build();

        try {
            repository.saveAndFlush(newCard);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMostSpecificCause().getMessage());
            throw new ConflictException("ERROR: Card already registered: " + dto.getCardNumber());
        }

        return CardRespDto.toDto(newCard);
    }

    @Transactional
    @Override
    public CardRespDto blockRequest(Long cardId, Authentication auth) {
        Long userId = extractUserId(auth);
        CreditCard card = repository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id: " + cardId)
        );

        if (!userId.equals(card.getUserId())) {
            log.error("Card {} block request by user {} has been rejected",
                    cardId, userId);
            throw new IllegalArgumentException("You can not block Card #" + cardId);
        }
        card.setToBlock(true);
        repository.saveAndFlush(card);
        return CardRespDto.toDto(card);
    }

    @Transactional
    public Boolean transfer(CardTransferDto dto, Authentication auth) {
        Long userId = extractUserId(auth);

        BigInteger amount = CardUtil.getAmountAsBigInteger(dto.getAmount());

        int withdrawn = repository.withdraw(
                dto.getFromId(),
                userId,
                amount,
                CreditCard.CardStatus.ACTIVE,
                false
                );
        if (withdrawn == 0) {
            log.error("Error transferring from Card: {}, amount: {}", dto.getFromId(), amount);
            throw new OperationRejectedException(
                    "Error transferring from Card: " + dto.getFromId() +
                            ", amount: " + amount);
        }

        int deposited = repository.deposit(
                dto.getToId(),
                userId,
                amount,
                CreditCard.CardStatus.ACTIVE,
                false
                );
        if (deposited == 0) {
            log.error("Error transferring to Card: {}", dto.getToId());
            throw new OperationRejectedException(
                    "Error transferring to Card: " + dto.getToId());
        }

        return true;
    }

    @Transactional
    @Override
    public void delete(Long cardId) {
        CreditCard card = repository.findByIdWithLock(cardId).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id: " + cardId)
        );

        if (card.getStatus() == CreditCard.CardStatus.ACTIVE) {
            throw new BusinessException("You can not delete ACTIVE Card #" + cardId);
        }

        repository.delete(card);
    }

    @Transactional
    @Override
    public CardRespDto changeStatus(Long id, CardStatusDto dto) {
        CreditCard found = repository.findByIdWithLock(id).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id: " + id)
        );

        found.changeStatus(dto.newStatus());

        return CardRespDto.toDto(found);
    }

    private Long extractUserId(Authentication auth){

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        if (!(auth.getPrincipal() instanceof UserDetails principal)) {
            throw new AccessDeniedException("Invalid authentication principal");
        }

        if (principal instanceof User user) {
            return user.getId();
        }

        throw new IllegalStateException("UserDetails does not expose user id");
    }

    @Override
    public String getTotalBalanceByUser(Authentication auth) {
        Long userId = extractUserId(auth);
        BigInteger sum = repository.sumBalanceByUserId(userId);
        return CardUtil.fromCentsToDecimal(sum).toPlainString();
    }
}
