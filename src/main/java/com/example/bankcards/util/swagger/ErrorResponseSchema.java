package com.example.bankcards.util.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ErrorResponse", description = "Unified error response")
public class ErrorResponseSchema {

    @Schema(example = "AUTHORIZATION_DENIED")
    public String type;

    @Schema(example = "Access denied")
    public String title;

    @Schema(example = "403")
    public Integer status;

    @Schema(example = "You are not authorized to access this resource")
    public String detail;

    @Schema(example = "/users/admin")
    public String path;

    @Schema(example = "2026-02-15T13:28:11Z")
    public String timestamp;

    @Schema(
            description = "Validation errors (only for VALIDATION_ERROR)",
            example = "{\"email\":\"must be a valid email\"}"
    )
    public Map<String, String> errors;
}

