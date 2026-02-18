package com.example.bankcards.util.swagger;

import com.example.bankcards.dto.CardRespDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CardPageResponse")
public class CardPageResponse extends PageResponse<CardRespDto> {
}
