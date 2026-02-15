package com.example.bankcards.controller;

import com.example.bankcards.dto.UserFilterDto;
import com.example.bankcards.dto.UserListDto;
import com.example.bankcards.dto.UserRespDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Bearer")
@Tag(name = "Users")
public class UserController {
    private final UserService service;

    @GetMapping("/me")
    @Operation(
            summary = "Получение данных текущего Юзера (для тестирования)"
    )
    public ResponseEntity<UserRespDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();
        UserRespDto dto = UserRespDto.toDto(currentUser);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/list") //TODO: описать для сваггера
    public Page<UserListDto> getUsers(
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

        return service.findAll(filter, pageable);
    }
}
