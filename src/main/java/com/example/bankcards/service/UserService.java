package com.example.bankcards.service;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserListDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserListDto> findAll(UserFilterDto filter, Pageable pageable);
    User findOne(Long userId);
    User update(Long userId, UserUpdateDto dto);
}
