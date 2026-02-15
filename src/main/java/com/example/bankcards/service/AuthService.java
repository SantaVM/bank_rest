package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserLoginRespDto;
import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.dto.UserRespDto;

public interface AuthService {
    UserRespDto register(UserRegisterDto dto);

    UserLoginRespDto login(UserLoginDto authRequest);
}
