package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.swagger.CardPageResponse;
import com.example.bankcards.util.swagger.CommonApiResponses;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Optional;

@RequestMapping("api/v1/cards")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Bearer")
@Tag(name = "Cards")
@CommonApiResponses
public class CardController {
    private final CardService service;

    @GetMapping("/admin/generate-card-number")
    @Operation(
            description = "Для целей тестирования. Генерирует валидный номер банковской карты (16 цифр) с контрольной по алгоритму Луна.",
            summary = "Генерация номера кредитной карты",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card Number",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<String> generate() {
        return ResponseEntity.ok(service.generate());
    }

    @PostMapping("/admin/create")
    @Operation(
            summary = "Регистрация Админом новой Карты для Юзера",
            description = """
          Номер карты валидируется по алгоритму Луна. Чтобы получить валидный номер (при тестировании),
          можно сгенерировать его с помощью эндпойнта 'cards/admin/generate-card-number'. Срок действия 
          новой карты должен быть не ранее текущей даты (карта не должна быть "просрочена"). 
          Баланс карты должен числом СТРОГО С ДВУМЯ ЗНАКАМИ после запятой.
          """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation =
                                    CardRespDto.class))),
            @ApiResponse(responseCode = "400", ref = "Validation"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    public ResponseEntity<CardRespDto> create(@Valid @RequestBody CardCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }


    @GetMapping("/admin/list")
    @Operation(
            summary = "Получение Админом списка всех карт (с фильтрацией и пагинацией)",
            description = "При отсутствии параметров фильтрации возвращается список ВСЕХ карт."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of cards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardPageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "Validation")
    })
    public ResponseEntity<Page<CardRespDto>> getCardsList(
            @Valid @ModelAttribute
            Optional<CardsListDto> dto,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getCardsList(dto.orElse(new CardsListDto()),
                pageable));
    }

    @GetMapping("/my-cards")
    @Operation(
            summary = "Получение Юзером списка принадлежащих ему карт (с фильтрацией и пагинацией)",
            description = "При отсутствии параметров фильтрации возвращается список ВСЕХ карт."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Page of cards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardPageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "Validation")
    })
    public ResponseEntity<Page<CardRespDto>> getAllForMe(
            @Valid @ModelAttribute
            Optional<CardHolderListDto> dto,
            Authentication auth,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<CardRespDto> fromDb =
                service.getUserCards(dto.orElse(new CardHolderListDto()), auth, pageable);
        return ResponseEntity.ok(fromDb);
    }

    @PatchMapping("/block/{cardId}")
    @Operation(
            summary = "Юзер помечает карту для блокировки Админом"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Операция успешно завершена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =
                                    CardRespDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public  ResponseEntity<CardRespDto> blockRequest(@PathVariable Long cardId,
                                                   Authentication auth) {
        return ResponseEntity.ok(service.blockRequest(cardId, auth));
    }

    @PostMapping("/transfer")
    @Operation(
            summary = "Операция перевода Юзером средств между своими картами"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Операция успешно завершена",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "Validation"),
    })
    public ResponseEntity<String> transfer(
            @Valid @RequestBody CardTransferDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(service.transfer(dto, auth) ? "Success" : "Failure");
    }


    @DeleteMapping("/admin/delete/{cardId}")
    @Operation(
            summary = "Админ может удалить BLOCKED или EXPIRED карту",
            description = "Удаляет карту и возвращает 204 No Content"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> delete(@PathVariable Long cardId) {
        service.delete(cardId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/change-status/{cardId}")
    @Operation(
            summary = "Админ может изменить статус карты. Если текущий статус" +
                    " EXPIRED, то изменить нельзя (получим исключение)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Операция успешно завершена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation =
                                    CardRespDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "Validation"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<CardRespDto> changeStatus(
            @Valid @RequestBody CardStatusDto dto,
            @PathVariable Long cardId
    ) {
        return ResponseEntity.ok(service.changeStatus(cardId, dto));
    }

    @GetMapping("/my-balance")
    @Operation(
            summary = "Получить сумму балансов всех карт текущего пользователя",
            description = "Возвращает сумму балансов всех карт пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Операция успешно завершена",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(responseCode = "200", description = "Сумма успешно получена"),
    })
    public ResponseEntity<String> getTotalBalance(Authentication auth) {
        return ResponseEntity.ok(service.getTotalBalanceByUser(auth));
    }
}
