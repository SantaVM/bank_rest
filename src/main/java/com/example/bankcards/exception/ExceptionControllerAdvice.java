package com.example.bankcards.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice {

    private final ErrorResponseFactory errorFactory;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ProblemDetail pd = errorFactory.createValidation(request, errors);
        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
            Exception ex,
            HttpServletRequest request
    ) {
        ProblemDetail pd = errorFactory.create(
                ErrorType.CONFLICT,
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );

        return ResponseEntity.status(pd.getStatus()).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleSecurityException(
            Exception ex,
            HttpServletRequest request
    ) {
        ProblemDetail errorDetail = null;

        // TODO send this stack trace to an observability tool
        ex.printStackTrace();

        if (ex instanceof BadCredentialsException) {
            errorDetail = errorFactory.create(
                    ErrorType.BAD_CREDENTIALS,
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage(),
                    request
            );
        }

        if (ex instanceof HttpMessageNotReadableException) {
            errorDetail = errorFactory.create(
                    ErrorType.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage(),
                    request
            );
        }

        if (ex instanceof NoSuchElementException || ex instanceof EntityNotFoundException) {
            errorDetail = errorFactory.create(
                    ErrorType.NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    ex.getMessage(),
                    request
            );
        }

        if (ex instanceof AccountStatusException) {
            errorDetail = errorFactory.create(
                        ErrorType.AUTHORIZATION_DENIED,
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage(),
                        request
                    );
        }

        if (ex instanceof IllegalArgumentException ||
                ex instanceof OperationRejectedException ||
                ex instanceof BusinessException ||
                ex instanceof AccessDeniedException) {
            errorDetail = errorFactory.create(
                    ErrorType.AUTHORIZATION_DENIED,
                    HttpStatus.FORBIDDEN,
                    ex.getMessage(),
                    request
            );
        }

        if (ex instanceof SignatureException || ex instanceof ExpiredJwtException || ex instanceof MalformedJwtException) {
            errorDetail = errorFactory.create(
                    ErrorType.AUTHENTICATION_REQUIRED,
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage(),
                    request
            );
        }

        if (errorDetail == null) {
            errorDetail = errorFactory.create(
                    ErrorType.INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected server error",
                    request
            );
        }

        return ResponseEntity.status(errorDetail.getStatus()).body(errorDetail);
    }
}
