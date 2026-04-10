package com.example.bankcards.util.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "ErrorResponseSchema", description = "Unified error response")
public class ErrorResponseSchema {

    @Schema(
            description = "The string containing a URI reference [URI] that identifies the problem type.",
            example = "about:blank")
    public String type;

    @Schema(example = "VALIDATION_ERROR")
    public String title;

    @Schema( description = "The number indicating the HTTP status code",
            example = "400")
    public Integer status;

    @Schema(example = "Validation failed")
    public String detail;

    @Schema(
            description = "The string containing a URI reference that identifies the specific occurrence of the problem",
            example = "/users/admin")
    public String instance;

    @Schema(
            description = "Validation errors (only for VALIDATION_ERROR)",
            example = "{\"email\":\"must be a valid email\"}"
    )
    public Map<String, String> errors;
}

