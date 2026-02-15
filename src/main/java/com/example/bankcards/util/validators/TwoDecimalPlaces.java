package com.example.bankcards.util.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TwoDecimalPlacesValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TwoDecimalPlaces {

    String message() default "Amount must have exactly two decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}