package com.example.bankcards.controller;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UserService userService;

    @Test
    void getAuthenticatedUser_Ok() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(5))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", List.of("USER"))
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        when(userService.findOne(any())).thenReturn(user);

        mockMvc.perform(
                get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService).findOne(any());
    }


    @Test
    @WithMockUser
    void getUsers_ForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/admin/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersWithoutAuthorization_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/admin/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_Ok() throws Exception {
        Page<UserRespDto> page = new PageImpl<>(
                List.of(
                        new UserRespDto(
                                1L,
                                "email@email.com",
                                User.Role.USER,
                                "firstName",
                                "lastName"
                                )
                )
        );

        when(userService.findAll(any(UserFilterDto.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(
                get("/api/v1/users/admin/list")
                        .param("page", "0")
                        .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(userService).findAll(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_validationError() throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/admin/list")
                                .param("email", "wrong_email")
                                .param("role", "WRONG_ROLE")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());

        verify(userService, never()).findAll(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ok() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .role(User.Role.USER)
                .firstName("firstName")
                .lastName("lastName")
                .build();
        UserRespDto dto = UserRespDto.toDto(user);

        when(userService.findOne(user.getId())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/admin/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(userService).findOne(user.getId());
    }

    @Test
    @WithMockUser
    void getUserById_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/admin/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ok() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("email@email.com");

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .role(User.Role.USER)
                .firstName("firstName")
                .lastName("lastName")
                .build();

        UserRespDto response = UserRespDto.toDto(user);

        when(userService.update(eq(1L), any(UserUpdateDto.class))).thenReturn(user);

        mockMvc.perform(patch("/api/v1/users/admin/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(userService).update(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_validationErrors() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("wrong_email");
        dto.setFirstName("wrong_name");

        mockMvc.perform(patch("/api/v1/users/admin/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(userService, never()).update(any(), any());
    }
}