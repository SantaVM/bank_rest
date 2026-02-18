package com.example.bankcards.controller;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.swagger.CommonApiResponses;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.swagger.UserPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/users")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Bearer")
@Tag(name = "Users")
@CommonApiResponses
public class UserController {
    private final UserService service;

    @GetMapping("/me")
    @Operation(
            summary = "Получение данных текущего Юзера (для тестирования)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRespDto.class)
                    )
            ),
    })
    public ResponseEntity<UserRespDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();
        UserRespDto dto = UserRespDto.toDto(currentUser);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/list")
    @Operation(
            summary = "Получение Админом списка Юзеров (с фильтрацией и пагинацией)",
            description = "При отсутствии параметров фильтрации возвращается список ВСЕХ Юзеров."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of users",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserPageResponse.class)
                    )
            ),
    })
    public ResponseEntity<Page<UserRespDto>> getUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) User.Role role,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        UserFilterDto filter = new UserFilterDto(
                firstName,
                lastName,
                email,
                role
        );

        Page<UserRespDto> usersPage = service.findAll(filter, pageable);

        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("/admin/{userId}")
    @Operation(
            summary = "Получение Админом данных Юзера по его ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRespDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", ref = "NotFound"), // Ссылка на
            // компонент
    })
    public ResponseEntity<UserRespDto> getUserById(@PathVariable Long userId) {
        User  user = service.findOne(userId);
        return ResponseEntity.ok(UserRespDto.toDto(user));
    }

    @PatchMapping("/admin/update/{userId}")
    @Operation(
            summary = "Изменение Админом данных Юзера по его ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRespDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "Validation"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict"),
    })
    public ResponseEntity<UserRespDto> updateUser(@PathVariable Long userId,
                                                  @Valid @RequestBody UserUpdateDto dto) {
        User user = service.update(userId, dto);
        return ResponseEntity.ok(UserRespDto.toDto(user));
    }
}
