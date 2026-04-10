package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.OperationRejectedException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CryptoUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {
    @Mock
    CardRepository repository;

    @Mock
    UserService userService;

    @Mock
    CryptoUtils cryptoUtils;

    @Mock
    CurrentUserService currentUser;

    @InjectMocks
    CardServiceImpl service;

    private User user;
    private CreditCard card;

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
    }

    @Test
    void getUserCards_shouldCreateSpecification() {
        CardHolderListDto dto = new CardHolderListDto();
        dto.setStatus(CreditCard.CardStatus.ACTIVE);
        dto.setToBlock(Boolean.FALSE);
        dto.setExpiryDate("12/29");

        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(cryptoUtils.decrypt(card.getCardNumber())).thenReturn(card.getCardNumber());

        //when
        service.getUserCards(dto, pageable);

        //then
        ArgumentCaptor<Specification<CreditCard>> captor = ArgumentCaptor.captor();
        verify(repository).findAll(captor.capture(), eq(pageable));

        Specification<CreditCard> spec = captor.getValue();

        assertThat(spec).isNotNull();
    }

    @Test
    void getUserCards_ok() {
        CardHolderListDto dto = new CardHolderListDto();
        dto.setStatus(CreditCard.CardStatus.ACTIVE);
        dto.setToBlock(Boolean.FALSE);
        dto.setExpiryDate("12/29");

        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(currentUser.getCurrentUserId()).thenReturn(1L);

        CardRespDto expected = CardRespDto.toDto(card, cryptoUtils);

        // when
        Page<CardRespDto> result = service.getUserCards(dto, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).toString()).isEqualTo(expected.toString());

        verify(currentUser).getCurrentUserId();
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getUserCards_withWrongDate_shouldThrowException() {
        CardHolderListDto dto = new CardHolderListDto();
        dto.setExpiryDate("wrong");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getUserCards(dto, PageRequest.of(0, 10))
        );

        assertTrue(ex.getMessage().contains("Invalid expiry date"));
    }

    @Test
    void getCardsList_ok() {
        CardsListDto dto = new CardsListDto();
        dto.setUserId(1L);
        dto.setCardHolder("JOHN");
        dto.setStatus(CreditCard.CardStatus.ACTIVE);
        dto.setToBlock(Boolean.FALSE);
        dto.setExpiryDate("12/29");

        Pageable pageable = PageRequest.of(0, 10);

        // given
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        CardRespDto expected = CardRespDto.toDto(card, cryptoUtils);

        // when
        Page<CardRespDto> result = service.getCardsList(dto, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).toString()).isEqualTo(expected.toString());

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getCardsList_shouldCreateSpecification() {
        CardsListDto dto = new CardsListDto();
        dto.setUserId(1L);
        dto.setCardHolder("JOHN");
        dto.setStatus(CreditCard.CardStatus.ACTIVE);
        dto.setToBlock(Boolean.FALSE);
        dto.setExpiryDate("12/29");

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        //when
        service.getCardsList(dto, PageRequest.ofSize(10));

        //then
        ArgumentCaptor<Specification<CreditCard>> captor = ArgumentCaptor.captor();
        verify(repository).findAll(captor.capture(), any(Pageable.class));

        Specification<CreditCard> spec = captor.getValue();

        assertThat(spec).isNotNull();
    }

    @Test
    void generate_shouldReturnCardNumber() {
        String number = service.generate();

        assertNotNull(number);
        assertFalse(number.isBlank());
    }

    @Test
    void getUserCards_shouldReturnUserCards_withDecryptedNumber() {
        // given
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        when(cryptoUtils.decrypt("1234567890123456"))
                .thenReturn("4111111111111111");

        // when
        Page<CardRespDto> result =
                service.getUserCards(new CardHolderListDto(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(CardRespDto::getCardNumber)
                .containsExactly("4111111111111111");

        verify(cryptoUtils).decrypt("1234567890123456");
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

        card.setToBlock(Boolean.TRUE);

        when(repository.saveAndFlush(any())).thenReturn(card);
        when(currentUser.getCurrentUserId()).thenReturn(1L);

        CardRespDto dto = service.blockRequest(10L);

        assertTrue(dto.getToBlock());
        verify(repository).saveAndFlush(card);
    }

    @Test
    void blockRequest_shouldRejectForeignCard() {
        User another = new User();
        another.setId(99L);

        card.setOwner(another);
        card.setUserId(another.getId());

        when(repository.findById(10L)).thenReturn(Optional.of(card));
        when(currentUser.getCurrentUserId()).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> service.blockRequest(10L));
    }

    @Test
    void blockRequest_notFound() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.blockRequest(10L));
    }

    @Test
    void transfer_shouldSucceed() {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("50.00"));

        when(currentUser.getCurrentUserId()).thenReturn(1L);

        when(repository.withdraw(
                eq(1L), eq(1L), any(), eq(CreditCard.CardStatus.ACTIVE), eq(false)))
                .thenReturn(1);

        when(repository.deposit(
                eq(2L), eq(1L), any(), eq(CreditCard.CardStatus.ACTIVE), eq(false)))
                .thenReturn(1);

        Boolean result = service.transfer(dto);

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

        OperationRejectedException ex = assertThrows(OperationRejectedException.class,
                () -> service.transfer(dto));
        assertTrue(ex.getMessage().contains("Error transferring from Card:"));
    }

    @Test
    void transfer_shouldFail_whenDepositFailed() {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("50.00"));

        when(repository.withdraw(any(), any(), any(), any(), any()))
            .thenReturn(1);

        when(repository.deposit(any(), any(), any(), any(), any()))
                .thenReturn(0);

        OperationRejectedException ex = assertThrows(OperationRejectedException.class,
                () -> service.transfer(dto));
        assertTrue(ex.getMessage().contains("Error transferring to Card:"));
    }

    @Test
    void delete_shouldSucceed() {
        card.setStatus(CreditCard.CardStatus.BLOCKED);

        when(repository.findByIdWithLock(1L))
                .thenReturn(Optional.of(card));
        doNothing().when(repository).delete(card);

        // Act & Assert - проверяем, что метод выполнился без исключений
        assertDoesNotThrow(() -> service.delete(1L));

        // Assert - проверяем, что delete был вызван с правильным аргументом
        verify(repository).findByIdWithLock(1L);
        verify(repository).delete(card);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_shouldRejectActiveCard() {

        when(repository.findByIdWithLock(1L))
                .thenReturn(Optional.of(card));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.delete(1L));
        assertTrue(ex.getMessage().contains("ACTIVE"));
    }

    @Test
    void delete_notFound() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.delete(1L));
        assertTrue(ex.getMessage().contains("Card not found"));

        verify(repository).findByIdWithLock(1L);
        verifyNoMoreInteractions(repository);
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
    void changeStatus_shouldFail_whenCardStatusIsExpired() {
        card.setStatus(CreditCard.CardStatus.EXPIRED);

        when(repository.findByIdWithLock(1L))
                .thenReturn(Optional.of(card));

        CardStatusDto dto = new CardStatusDto(CreditCard.CardStatus.ACTIVE);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus(1L, dto)
        );
        assertTrue(ex.getMessage().contains("Cannot change status"));
    }

    @Test
    void changeStatus_notFound() {
        CardStatusDto dto = new CardStatusDto(CreditCard.CardStatus.ACTIVE);

        when(repository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.changeStatus(1L, dto));
        assertTrue(ex.getMessage().contains("Card not found"));
    }

    @Test
    void getTotalBalanceByUser_shouldReturnDecimalString() {
        when(repository.sumBalanceByUserId(1L))
                .thenReturn(BigInteger.valueOf(12345));
        when(currentUser.getCurrentUserId()).thenReturn(1L);

        String result = service.getTotalBalanceByUser();

        assertEquals("123.45", result);
    }

}
