package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardRespDto;
import com.example.bankcards.dto.CardStatusDto;
import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.exception.ErrorResponseFactory;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private ErrorResponseFactory errorResponseFactory;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {

        // Мокаем JwtAuthenticationFilter так, чтобы он просто пропускал запрос дальше
        Mockito.doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter)
                .doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generateCardNumber_ok() throws Exception {
        when(cardService.generate()).thenReturn("4111111111111111");

        mockMvc.perform(get("/api/v1/cards/admin/generate-card-number"))
                .andExpect(status().isOk())
                .andExpect(content().string("4111111111111111"));

        verify(cardService).generate();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_created() throws Exception {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setCardNumber("4000006806224829");
        dto.setExpiryDate("12/50");
        dto.setBalance(new BigDecimal("1000.00"));

        CardRespDto response = new CardRespDto();
        response.setId(1L);

        when(cardService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/cards/admin/create")
                        .with(csrf())                       // <-- вот это важно
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cardService).create(any(CardCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_validationError() throws Exception {
        CardCreateDto dto = new CardCreateDto();
        dto.setBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/v1/cards/admin/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardsList_ok() throws Exception {
        Page<CardRespDto> page =
                new PageImpl<>(List.of(new CardRespDto()));

        when(cardService.getCardsList(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/admin/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService).getCardsList(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getMyCards_ok() throws Exception {
        Page<CardRespDto> page =
                new PageImpl<>(List.of(new CardRespDto()));

        when(cardService.getUserCards(any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/my-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService)
                .getUserCards(any(), any(Authentication.class), any());
    }

    @Test
    @WithMockUser
    void blockRequest_ok() throws Exception {
        CardRespDto dto = new CardRespDto();
        dto.setId(10L);

        when(cardService.blockRequest(eq(10L), any()))
                .thenReturn(dto);

        mockMvc.perform(patch("/api/v1/cards/block/10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(cardService).blockRequest(eq(10L), any());
    }

    @Test
    @WithMockUser
    void transfer_success() throws Exception {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("1000.00"));

        when(cardService.transfer(any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));

        verify(cardService).transfer(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_noContent() throws Exception {
        doNothing().when(cardService).delete(5L);

        mockMvc.perform(delete("/api/v1/cards/admin/delete/5")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService).delete(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeStatus_ok() throws Exception {
        CardStatusDto dto = new CardStatusDto(CreditCard.CardStatus.ACTIVE);
        CardRespDto resp = new CardRespDto();
        resp.setId(3L);

        when(cardService.changeStatus(eq(3L), any()))
                .thenReturn(resp);

        mockMvc.perform(patch("/api/v1/cards/admin/change-status/3")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L));

        verify(cardService).changeStatus(eq(3L), any());
    }

    @Test
    @WithMockUser
    void getTotalBalance_ok() throws Exception {
        when(cardService.getTotalBalanceByUser(any()))
                .thenReturn("1500.50");

        mockMvc.perform(get("/api/v1/cards/my-balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.50"));

        verify(cardService).getTotalBalanceByUser(any());
    }


}
