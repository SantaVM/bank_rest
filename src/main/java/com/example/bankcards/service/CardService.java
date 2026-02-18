package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface CardService {
    Page<CardRespDto> getUserCards(CardHolderListDto filter,
                                   Authentication auth, Pageable pageable);
    String generate();
    CardRespDto create(CardCreateDto dto);
    Page<CardRespDto> getCardsList(CardsListDto filter, Pageable pageable);
    CardRespDto blockRequest(Long cardId, Authentication auth);
    Boolean transfer(CardTransferDto dto, Authentication auth);
    void delete(Long cardId);
    CardRespDto changeStatus(Long cardId, CardStatusDto dto);
    String getTotalBalanceByUser(Authentication auth);
}
