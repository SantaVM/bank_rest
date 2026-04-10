package com.example.bankcards.controller;

import com.example.bankcards.config.AuthConfig;
import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserLoginRespDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.AuthServiceImpl;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, CardController.class})
@Import({AuthConfig.class, SecurityConfig.class, AuthServiceImpl.class, JwtService.class})
class JwtAuthTestIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @MockitoBean
    private CardService cardService;

    @Test
    @DisplayName("Проверка успешной генерации JWT токена и работы JwtAuthenticationProvider")
    void loginAsAdmin_AndRequestCardNumber_Success() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("email@email.com");
        dto.setPassword("password");

        User adminUser = User.builder()
            .id(1L)
            .email("email@email.com")
                .password(encoder.encode("password"))
            .role(User.Role.ADMIN)
            .build();

        when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.of(adminUser));
        when(cardService.generate()).thenReturn("4111111111111111");

        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andReturn();

        String respBody = result.getResponse().getContentAsString();
        UserLoginRespDto loginRespDto = objectMapper.readValue(respBody, UserLoginRespDto.class);

        String token = loginRespDto.token();

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(1L, loginRespDto.userId());

        System.out.println(token);

        mockMvc.perform(
                get("/api/v1/cards/admin/generate-card-number")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("4111111111111111"));

        verify(repository).findByEmail(dto.getEmail());
        verify(cardService).generate();
    }

    @Test
    void loginAsUser_AndRequestCardNumber_Forbidden() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("email@email.com");
        dto.setPassword("password");

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password(encoder.encode("password"))
                .role(User.Role.USER)
                .build();

        when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(cardService.generate()).thenReturn("4111111111111111");

        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andReturn();

        String respBody = result.getResponse().getContentAsString();

        UserLoginRespDto loginRespDto = objectMapper.readValue(respBody, UserLoginRespDto.class);

        String token = loginRespDto.token();

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(1L, loginRespDto.userId());

        mockMvc.perform(
                get("/api/v1/cards/admin/generate-card-number")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());

        verify(repository).findByEmail(dto.getEmail());
        verify(cardService, never()).generate();
    }

    @Test
    void getWithWrongToken_Unauthorized() throws Exception {
        mockMvc.perform(
                get("/api/v1/cards/my-balance")
                .header("Authorization", "Bearer wrong.token")
        )
        .andExpect(status().isUnauthorized());
    }
}