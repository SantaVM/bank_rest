package com.example.bankcards.service;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Page<UserRespDto> findAll(UserFilterDto filter, Pageable pageable) {

        Specification<User> specification = Specification.unrestricted();

        if (filter.firstName() != null && !filter.firstName().isBlank()) {
            specification = specification.and(
                    UserSpecifications.firstNameLike(filter.firstName()));
        }

        if (filter.lastName() != null && !filter.lastName().isBlank()) {
            specification = specification.and(
                    UserSpecifications.lastNameLike(filter.lastName()));
        }

        if (filter.email() != null && !filter.email().isBlank()) {
            specification = specification.and(
                    UserSpecifications.emailLike(filter.email()));
        }

        if (filter.role() != null) {
            specification = specification.and(
                    UserSpecifications.hasRole(filter.role()));
        }

        // если specification = null → вернутся все пользователи
        return repository.findAll(specification, pageable).map(UserRespDto::toDto);
    }

    @Override
    public User findOne(Long userId) {
        return repository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with " +
                        "id: " + userId)
        );
    }

    @Override
    @Transactional
    public User update(Long userId, UserUpdateDto dto) {

        User user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Optional.ofNullable(dto.getEmail())
                    .ifPresent(user::setEmail);

        Optional.ofNullable(dto.getFirstName())
                .ifPresent(user::setFirstName);

        Optional.ofNullable(dto.getLastName())
                .ifPresent(user::setLastName);

        Optional.ofNullable(dto.getRole()).ifPresent(user::setRole);

        try {
            repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email already exists: " + dto.getEmail());
        }

        return user;
    }
}
