package com.example.bankcards.controller;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserLoginRespDto;
import com.example.bankcards.dto.UserRegisterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_success() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("email@email.com");
        dto.setPassword("password");

        UserLoginRespDto resp = new UserLoginRespDto(1L, "Token");

        when(authService.login(dto)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.token").value("Token"));

        verify(authService, times(1)).login(dto);
    }

    @Test
    void login_validationError() throws Exception {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("wrong email");
        dto.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        )
                .andDo(result -> {
                    String json = result.getResponse().getContentAsString();
                    Object jsonObj = objectMapper.readValue(json, Object.class);
                    System.out.println(
                            objectMapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(jsonObj)
                    );
                })
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never()).login(any());
    }

    @Test
    void register_success() throws Exception {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("email@email.com")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        UserRespDto resp = new UserRespDto(
                1L,
                "email@email.com",
                User.Role.USER,
                "firstName",
                "lastName"
        );

        when(authService.register(dto)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                            )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        objectMapper.writeValueAsString(resp)
                ));

        verify(authService, times(1)).register(dto);
    }

    @Test
    void register_validationError() throws Exception {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("wrong email")
                .password("short")
                .firstName("wr0ng")
                .lastName("wr0ng")
                .build();

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        )
                .andDo(result -> {
                    String json = result.getResponse().getContentAsString();
                    Object jsonObj = objectMapper.readValue(json, Object.class);
                    System.out.println(
                            objectMapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(jsonObj)
                    );
                })
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never()).register(any());
    }
}
