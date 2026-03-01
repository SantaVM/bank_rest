package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class UserTokenDto {
    private Long id;
    private String email;
    private String role;

    public static UserDetails toUserDetails(Claims claims) {
        return User.builder()
                .id(claims.get("userId", Long.class))
                .email(claims.getSubject())
                .password("")
                .role(User.Role.valueOf(claims.get("role", String.class)))
                .build();
    }

    public static UserTokenDto fromUser(User user) {
        UserTokenDto dto = new UserTokenDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public Map<String, Object> getExtraClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", id);
        claims.put("role", role);
        return claims;
    }
}
