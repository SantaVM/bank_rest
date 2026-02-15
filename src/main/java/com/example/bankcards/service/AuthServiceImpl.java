package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserLoginRespDto;
import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserRespDto register(UserRegisterDto dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("ERROR: Email already registered:" + dto.getEmail());
        }
        User newUser = User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .role(User.Role.USER)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();
        repository.saveAndFlush(newUser);
        return UserRespDto.toDto(newUser);
    }

    public UserLoginRespDto login(UserLoginDto authRequest){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));
        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user.getEmail());
        return new UserLoginRespDto(user.getId(), token);
    }
}
