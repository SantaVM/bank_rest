package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardRespDto;
import com.example.bankcards.dto.CardStatusDto;
import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.OperationRejectedException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {
    @Mock
    CardRepository repository;

    @Mock
    UserService userService;

    @InjectMocks
    CardServiceImpl service;

    Authentication auth;
    User user;
    CreditCard card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        card = CreditCard.builder()
                .id(10L)
                .owner(user)
                .userId(user.getId())
                .cardHolder("JOHN DOE")
                .cardNumber("1234567890123456")
                .expiryDate(LocalDate.of(2029, 12, 1))
                .balance(BigInteger.valueOf(10_000))
                .status(CreditCard.CardStatus.ACTIVE)
                .toBlock(false)
                .build();

        auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generate_shouldReturnCardNumber() {
        String number = service.generate();

        assertNotNull(number);
        assertFalse(number.isBlank());
    }

    @Test
    void create_shouldCreateCard() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setCardNumber("1234567890123456");
        dto.setExpiryDate("12/29");
        dto.setBalance(new BigDecimal("100.00"));

        when(userService.findOne(1L)).thenReturn(user);
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CardRespDto result = service.create(dto);

        assertEquals("JOHN DOE", result.getCardHolder());
        verify(repository).saveAndFlush(any(CreditCard.class));
    }

    @Test
    void create_shouldThrowConflict_whenDuplicateCard() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setCardNumber("1234567890123456");
        dto.setExpiryDate("12/29");
        dto.setBalance(new BigDecimal("100.00"));

        when(userService.findOne(1L)).thenReturn(user);
        when(repository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(ConflictException.class, () -> service.create(dto));
    }

    @Test
    void blockRequest_shouldBlockCard() {

        when(repository.findById(10L)).thenReturn(Optional.of(card));
        when(repository.saveAndFlush(any())).thenReturn(card);

        CardRespDto dto = service.blockRequest(10L, auth);

        assertTrue(card.getToBlock());
        verify(repository).saveAndFlush(card);
    }

    @Test
    void blockRequest_shouldRejectForeignCard() {
        User another = new User();
        another.setId(99L);

        card.setOwner(another);
        card.setUserId(another.getId());

        when(repository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(IllegalArgumentException.class,
                () -> service.blockRequest(10L, auth));
    }

    @Test
    void transfer_shouldSucceed() {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("50.00"));

        when(repository.withdraw(
                eq(1L), eq(1L), any(), eq(CreditCard.CardStatus.ACTIVE), eq(false)))
                .thenReturn(1);

        when(repository.deposit(
                eq(2L), eq(1L), any(), eq(CreditCard.CardStatus.ACTIVE), eq(false)))
                .thenReturn(1);

        Boolean result = service.transfer(dto, auth);

        assertTrue(result);
    }

    @Test
    void transfer_shouldFail_whenWithdrawRejected() {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("50.00"));

        when(repository.withdraw(any(), any(), any(), any(), any()))
                .thenReturn(0);

        assertThrows(OperationRejectedException.class,
                () -> service.transfer(dto, auth));
    }

    @Test
    void delete_shouldRejectActiveCard() {
        CreditCard card = new CreditCard();
        card.setStatus(CreditCard.CardStatus.ACTIVE);

        when(repository.findByIdWithLock(1L))
                .thenReturn(Optional.of(card));

        assertThrows(BusinessException.class,
                () -> service.delete(1L));
    }

    @Test
    void changeStatus_shouldUpdateStatus() {
        card.setStatus(CreditCard.CardStatus.BLOCKED);

        when(repository.findByIdWithLock(1L))
                .thenReturn(Optional.of(card));

        CardStatusDto dto = new CardStatusDto(CreditCard.CardStatus.ACTIVE);

        CardRespDto result = service.changeStatus(1L, dto);

        assertEquals(CreditCard.CardStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getTotalBalanceByUser_shouldReturnDecimalString() {
        when(repository.sumBalanceByUserId(1L))
                .thenReturn(BigInteger.valueOf(12345));

        String result = service.getTotalBalanceByUser(auth);

        assertEquals("123.45", result);
    }

}
