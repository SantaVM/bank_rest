package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class ErrorResponseFactory {

    public static ProblemDetail create(
            ErrorType type,
            HttpStatus status,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(type.name());
        pd.setInstance(URI.create(request.getRequestURI()));
        return pd;
    }

    public static ProblemDetail createValidation(
            HttpServletRequest request,
            Map<String, String> errors
    ) {
        ProblemDetail pd = create(
                ErrorType.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request
        );
        pd.setProperty("errors", errors);
        return pd;
    }
}

