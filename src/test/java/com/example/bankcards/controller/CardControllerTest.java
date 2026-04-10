package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardRespDto;
import com.example.bankcards.dto.CardStatusDto;
import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.entity.CreditCard;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import({SecurityConfig.class})
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void generateCardNumber_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/cards/admin/generate-card-number"))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateCardNumber_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cards/admin/generate-card-number"))
                .andExpect(status().isUnauthorized());
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cardService).create(any(CardCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ShouldMaskCardNumber() throws Exception {
        String originalCardNumber = "4000006806224829";
        String expectedMaskedNumber = "**** **** **** 4829";

        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setCardNumber(originalCardNumber);
        dto.setExpiryDate("12/50");
        dto.setBalance(new BigDecimal("1000.00"));

        CardRespDto responseDto = new CardRespDto();
        responseDto.setId(1L);
        responseDto.setCardNumber(originalCardNumber); // Оригинальный номер
        responseDto.setStatus(CreditCard.CardStatus.ACTIVE);

        when(cardService.create(any(CardCreateDto.class))).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(post("/api/v1/cards/admin/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andReturn();

        // Получаем JSON ответ и проверяем маскирование
        String responseJson = result.getResponse().getContentAsString();
        CardRespDto actualResponse = objectMapper.readValue(responseJson, CardRespDto.class);

        // Проверяем, что номер карты замаскирован
        assertThat(actualResponse.getCardNumber())
                .isNotEqualTo(originalCardNumber)
                .isEqualTo(expectedMaskedNumber)
                .matches("^\\*{4} \\*{4} \\*{4} \\d{4}$"); // Проверка формата
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_validationError() throws Exception {
        CardCreateDto dto = new CardCreateDto();
        dto.setBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/v1/cards/admin/create")
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
    @WithMockUser(roles = "ADMIN")
    void getCardsList_ShouldMaskCardNumbers() throws Exception {
        CardRespDto dto_1 = new CardRespDto();
        dto_1.setCardNumber("4000006806224828");
        String expectedMaskedNumber1 = "**** **** **** 4828";
        CardRespDto dto_2 = new CardRespDto();
        dto_2.setCardNumber("4000006806224829");
        String expectedMaskedNumber2 = "**** **** **** 4829";

        Page<CardRespDto> page = new PageImpl<>(List.of(dto_1, dto_2), PageRequest.of(0, 10), 2);

        when(cardService.getCardsList(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/admin/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardNumber").value(expectedMaskedNumber1))
                .andExpect(jsonPath("$.content[1].cardNumber").value(expectedMaskedNumber2));

        verify(cardService).getCardsList(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardsList_validationError() throws Exception {
        mockMvc.perform(get("/api/v1/cards/admin/list")
                        .param("userId", "0")
                        .param("cardHolder", "wrong_name")
                        .param("expiryDate", "wrong_date")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors").isNotEmpty())
        .andExpect(jsonPath("$.errors.expiryDate").isNotEmpty())
        .andExpect(jsonPath("$.errors.cardHolder").isNotEmpty())
        .andExpect(jsonPath("$.errors.userId").isNotEmpty());

        verify(cardService, never()).getCardsList(any(), any());
    }

    @Test
    @WithMockUser
    void getAllForMe_ok() throws Exception {
        Page<CardRespDto> page =
                new PageImpl<>(List.of(new CardRespDto()));

        when(cardService.getUserCards(any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/my-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(cardService)
                .getUserCards(any(), any());
    }

    @Test
    @WithMockUser
    void getAllForMe_ShouldMaskCardNumbers() throws Exception {
        CardRespDto dto_1 = new CardRespDto();
        dto_1.setCardNumber("4000006806224828");
        String expectedMaskedNumber1 = "**** **** **** 4828";
        CardRespDto dto_2 = new CardRespDto();
        dto_2.setCardNumber("4000006806224829");
        String expectedMaskedNumber2 = "**** **** **** 4829";

        Page<CardRespDto> page = new PageImpl<>(List.of(dto_1, dto_2), PageRequest.of(0, 10), 2);

        when(cardService.getUserCards(any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cards/my-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardNumber").value(expectedMaskedNumber1))
                .andExpect(jsonPath("$.content[1].cardNumber").value(expectedMaskedNumber2));

        verify(cardService)
                .getUserCards(any(), any());
    }

    @Test
    @WithMockUser
    void getAllForMe_validationError() throws Exception {

        mockMvc.perform(get("/api/v1/cards/my-cards")
                        .param("expiryDate", "wrong_date")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());

        verify(cardService, never()).getUserCards(any(), any());
    }

    @Test
    @WithMockUser
    void blockRequest_ok() throws Exception {
        CardRespDto dto = new CardRespDto();
        dto.setId(10L);

        when(cardService.blockRequest(eq(10L)))
                .thenReturn(dto);

        mockMvc.perform(patch("/api/v1/cards/block/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(cardService).blockRequest(eq(10L));
    }

    @Test
    @WithMockUser
    void transfer_success() throws Exception {
        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(1L);
        dto.setToId(2L);
        dto.setAmount(new BigDecimal("1000.00"));

        when(cardService.transfer(any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));

        verify(cardService).transfer(any());
    }

    @Test
    @WithMockUser
    void transfer_whenTransferToTheSameId_validationError() throws Exception {
        Long cardId = 10L;

        CardTransferDto dto = new CardTransferDto();
        dto.setFromId(cardId);
        dto.setToId(cardId);
        dto.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isNotEmpty())
            .andExpect(jsonPath("$.errors.global").value("Field 'fromId' can't be equal to 'toId'"));

        verify(cardService, never()).transfer(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_noContent() throws Exception {
        doNothing().when(cardService).delete(5L);

        mockMvc.perform(delete("/api/v1/cards/admin/delete/5"))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L));

        verify(cardService).changeStatus(eq(3L), any());
    }

    @Test
    @WithMockUser
    void getTotalBalance_ok() throws Exception {
        when(cardService.getTotalBalanceByUser())
                .thenReturn("1500.50");

        mockMvc.perform(get("/api/v1/cards/my-balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.50"));

        verify(cardService).getTotalBalanceByUser();
    }


}
