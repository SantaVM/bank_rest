package com.example.bankcards.service;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(User.Role.USER);
    }

    @Test
    void findAll_ok() {
        UserFilterDto dto = new UserFilterDto(
                "John",
                "Dow",
                "email@email.com",
                User.Role.USER
        );

        Pageable pageable = PageRequest.of(0, 10);
        UserRespDto expected = UserRespDto.toDto(user);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));

        // when
        Page<UserRespDto> actual = service.findAll(dto, pageable);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getContent()).hasSize(1);
        assertThat(actual.getContent().get(0).toString()).isEqualTo(expected.toString());

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_shouldCreateSpecification() {
        UserFilterDto dto = new UserFilterDto(
                "John",
                "Dow",
                "email@email.com",
                User.Role.USER
        );
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        // when
        service.findAll(dto, pageable);

        // then
        ArgumentCaptor<Specification<User>> captor = ArgumentCaptor.captor();
        verify(repository).findAll(captor.capture(),  eq(pageable));
        Specification<User> specification = captor.getValue();

        assertThat(specification).isNotNull();
    }

    @Test
    void findOne_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User actual = service.findOne(1L);

        assertThat(actual).isEqualTo(user);
    }

    @Test
    void findOne_shouldThrowNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex =  assertThrows(EntityNotFoundException.class, () -> service.findOne(1L));
        assertThat(ex.getMessage()).contains("User not found");
    }

    @Test
    void update_success() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFirstName("NewName");
        dto.setLastName("NewLastName");
        dto.setEmail("new@new.com");
        dto.setRole(User.Role.ADMIN);

        User expected = User.builder()
                .id(1L)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User actual = service.update(1L, dto);

        assertThat(actual.toString()).isEqualTo(user.toString());

        verify(repository).findById(1L);
        verify(repository).saveAndFlush(expected);
    }

    @Test
    void update_shouldThrowNotFoundException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex =  assertThrows(EntityNotFoundException.class,
                () -> service.update(1L, new UserUpdateDto()));
        assertThat(ex.getMessage()).contains("User not found");
    }

    @Test
    void update_shouldThrowConflictException() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.saveAndFlush(user)).thenThrow(new DataIntegrityViolationException("duplicate"));

        ConflictException ex =   assertThrows(ConflictException.class,
                () -> service.update(1L, new UserUpdateDto()));

        assertThat(ex.getMessage()).contains("Email already exists:");
    }
}