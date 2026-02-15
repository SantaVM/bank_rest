package com.example.bankcards.security;

import com.example.bankcards.exception.ErrorResponseFactory;
import com.example.bankcards.exception.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseFactory errorFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        ProblemDetail pd = errorFactory.create(
                ErrorType.AUTHORIZATION_DENIED,
                HttpStatus.FORBIDDEN,
                "You are not authorized to access this resource",
                request
        );

        write(response, pd);
    }

    private void write(HttpServletResponse response, ProblemDetail pd) throws IOException {
        response.setStatus(pd.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }
}