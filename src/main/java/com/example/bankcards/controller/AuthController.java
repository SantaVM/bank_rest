package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.util.swagger.ErrorResponseSchema;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1/auth")
@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService service;

    @PostMapping("/login")
    @Operation(
            description = "Get user ID and JWT token",
            summary = "Login with email and password",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserLoginRespDto.class) )
                    ),
                    @ApiResponse(responseCode = "401", ref = "401")
            }
    )
    public ResponseEntity<UserLoginRespDto> login(@Valid @RequestBody UserLoginDto authRequest){
        UserLoginRespDto authResponse = service.login(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-up")
    @Operation(
            summary = "Создаётся новый Юзер",
            description = """
          Роль нового юзера по умолчанию: 'USER'. Имя и фамилия автоматически используются
          в поле 'CARDHOLDER' для карты, поэтому должны содержать только латинские буквы.
          """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "User added successfully",
                    content = @Content(mediaType = "application/json",
                            schema =
                            @Schema(implementation = UserRespDto.class))
            ),
            @ApiResponse(responseCode = "400", ref = "Validation"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto dto){
        UserRespDto regUser = service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(regUser);
    }
}