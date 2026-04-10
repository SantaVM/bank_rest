package com.example.bankcards.security;

import com.example.bankcards.exception.ErrorResponseFactory;
import com.example.bankcards.exception.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class CustomTokenAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param request               that resulted in an <code>AccessDeniedException</code>
     * @param response              so that the user agent can be advised of the failure
     * @param ex                    that caused the invocation
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        log.error("Access denied for request {}", request.getRequestURI());

        Map<String, String> parameters = new LinkedHashMap<>();

        if (request.getUserPrincipal() instanceof AbstractOAuth2TokenAuthenticationToken) {
            parameters.put("error", "insufficient_scope");
            parameters.put("error_description", "The request requires higher privileges than provided by the access token.");
        }

        String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
        response.addHeader("WWW-Authenticate", wwwAuthenticate);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ProblemDetail pd = ErrorResponseFactory.create(
                ErrorType.AUTHORIZATION_DENIED,
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }

    private static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
        StringBuilder wwwAuthenticate = new StringBuilder();
        wwwAuthenticate.append("Bearer");
        if (!parameters.isEmpty()) {
            wwwAuthenticate.append(" ");
            int i = 0;

            for(Map.Entry<String, String> entry : parameters.entrySet()) {
                wwwAuthenticate.append((String)entry.getKey()).append("=\"").append((String)entry.getValue()).append("\"");
                if (i != parameters.size() - 1) {
                    wwwAuthenticate.append(", ");
                }

                ++i;
            }
        }

        return wwwAuthenticate.toString();
    }
}
