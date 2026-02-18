package com.example.bankcards.dto;

import com.example.bankcards.util.validators.TwoDecimalPlaces;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CardTransferDto {
    @NotNull
    @Min(1)
    @Schema(example = "2")
    private Long fromId;

    @NotNull
    @Min(1)
    @Schema(example = "1")
    private Long toId;

    @TwoDecimalPlaces
    @NotNull
    @Schema(example = "1234.56")
    private BigDecimal amount;
}
