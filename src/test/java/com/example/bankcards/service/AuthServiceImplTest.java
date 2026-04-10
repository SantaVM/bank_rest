package com.example.bankcards.service;


import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserLoginRespDto;
import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("email@email.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPassword");

        UserRespDto result = authService.register(dto);

        // проверяем результат
        assertNotNull(result);
        assertEquals("email@email.com", result.email());
        assertEquals(User.Role.USER, result.role());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());

        // проверяем взаимодействия
        verify(repository).existsByEmail("email@email.com");
        verify(encoder).encode("password");
        verify(repository).saveAndFlush(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsConflict() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("email@email.com")
                .password("password")
                .build();

        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> authService.register(dto)
        );

        assertTrue(ex.getMessage().contains("Email already registered"));

        verify(repository).existsByEmail(dto.getEmail());
        verify(repository, never()).saveAndFlush(any());
        verify(encoder, never()).encode(any());
    }

    @Test
    void register_savesCorrectUser() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("email@email.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encoded");

        authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("email@email.com", savedUser.getEmail());
        assertEquals("encoded", savedUser.getPassword());
        assertEquals(User.Role.USER, savedUser.getRole());
    }

    @Test
    void login_success() {
        UserLoginDto dto = new UserLoginDto();
        dto.setPassword("password");
        dto.setEmail("email@email.com");

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .role(User.Role.USER)
                .build();

        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        UserLoginRespDto result = authService.login(dto);

        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("jwt-token", result.token());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_authenticationFails_throwsException() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("email@email.com");
        dto.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(dto)
        );

        verify(jwtService, never()).generateToken(any());
    }
}
