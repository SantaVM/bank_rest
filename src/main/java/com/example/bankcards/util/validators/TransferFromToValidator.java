package com.example.bankcards.util.validators;

import com.example.bankcards.dto.CardTransferDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransferFromToValidator implements ConstraintValidator<ValidFromToTransfer, CardTransferDto> {
    @Override
    public void initialize(ValidFromToTransfer constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(CardTransferDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (dto == null) {
            return true;
        }

        if (dto.getFromId() == null || dto.getToId() == null) {
            return true; // null обрабатываем отдельными @NotNull
        }

        return !dto.getFromId().equals(dto.getToId());
    }
}
