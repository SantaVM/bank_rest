package com.example.bankcards.util.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExpirationDateValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpirationDate {
    String message() default "Некорректный срок действия карты (MM/YY)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
