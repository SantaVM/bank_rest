package com.example.bankcards.service;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    public Page<UserListDto> findAll(UserFilterDto filter, Pageable pageable);
}
