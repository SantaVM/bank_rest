package com.example.bankcards.util.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TwoDecimalPlacesValidator implements ConstraintValidator<TwoDecimalPlaces, BigDecimal> {
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Обрабатывается отдельно через @NotNull
        }

        return value.scale() == 2;
    }
}
