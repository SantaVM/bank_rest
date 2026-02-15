package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class ErrorResponseFactory {

    public ProblemDetail create(
            ErrorType type,
            HttpStatus status,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(type.name());
        pd.setProperty("type", type.name());
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    public ProblemDetail createValidation(
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

