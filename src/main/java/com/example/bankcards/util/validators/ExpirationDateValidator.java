package com.example.bankcards.util.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ExpirationDateValidator implements ConstraintValidator<ValidExpirationDate, String> {

    @Override
    public boolean isValid(String expDate, ConstraintValidatorContext context) {
        if (expDate == null || !expDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) return false;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth cardDate = YearMonth.parse(expDate, formatter);
            YearMonth now = YearMonth.now();
            return cardDate.isAfter(now) || cardDate.equals(now);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

