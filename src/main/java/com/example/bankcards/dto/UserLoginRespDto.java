package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//@Data
//@AllArgsConstructor
//public class UserLoginRespDto {
//    private Long userId;
//    private String token;
//}

public record UserLoginRespDto(Long userId, String token) {}
