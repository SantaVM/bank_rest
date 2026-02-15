package com.example.bankcards.service;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserListDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Page<UserListDto> findAll(UserFilterDto filter, Pageable pageable) {

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
        return repository.findAll(specification, pageable).map(UserListDto::toDto);
    }
}
