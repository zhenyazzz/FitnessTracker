package org.example.fitnesstracker.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.dto.response.ErrorResponse;
import org.example.fitnesstracker.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media", description = "API для работы с медиа-данными")
public class MediaController {
    private final MediaService mediaService;

    @Operation(
        summary = "Получение списка всех медиа-данных",
        description = "Получает список всех медиа-данных пользователя с возможностью фильтрации и сортировки",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameters({
        @Parameter(name = "sortBy", description = "Поле для сортировки (createdAt, fileSize, mimeType, note)", example = "createdAt"),
        @Parameter(name = "sortDirection", description = "Направление сортировки (asc, desc)", example = "desc"),
        @Parameter(name = "dateFrom", description = "Начальная дата фильтрации (формат: YYYY-MM-DD)", example = "2024-12-01"),
        @Parameter(name = "dateTo", description = "Конечная дата фильтрации (формат: YYYY-MM-DD)", example = "2024-12-31"),
        @Parameter(name = "page", description = "Номер страницы (начиная с 0)", example = "0"),
        @Parameter(name = "size", description = "Размер страницы (от 1 до 100)", example = "10")
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список медиа-данных успешно получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Не авторизован",
                    value = "{\n  \"message\": \"Unauthorized\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Внутренняя ошибка",
                    value = "{\n  \"message\": \"Internal server error\",\n  \"status\": 500,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<MediaResponse>> getAllMedia(
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection,
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size

    ) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            log.warn("Invalid date range: dateFrom ({}) is after dateTo ({})", dateFrom, dateTo);
            throw new IllegalArgumentException("dateFrom must be before or equal to dateTo");
        }
        
        log.debug("Request to get all media with filters: sortBy={}, sortDirection={}, dateFrom={}, dateTo={}, page={}, size={}", 
            sortBy, sortDirection, dateFrom, dateTo, page, size);
        Page<MediaResponse> result = mediaService.getAllMedia(sortBy, sortDirection, dateFrom, dateTo, page, size);
        log.info("Successfully retrieved {} media items (page {}, total: {})", 
            result.getContent().size(), page, result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Получение медиа-данных по ID",
        description = "Получает медиа-данные по ID. Возвращает presigned URL для доступа к файлу.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID медиа-данных для получения", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Медиа-данные успешно получены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MediaResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"presignedUrl\": \"http://localhost:9000/test-bucket/uuid-generated-path.jpg?signature=abc123\",\n  \"note\": \"Фото прогресса\",\n  \"fileSize\": 1024,\n  \"mimeType\": \"image/jpeg\",\n  \"createdAt\": \"2024-12-15T10:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Не авторизован",
                    value = "{\n  \"message\": \"Unauthorized\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Медиа-данные не найдены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Медиа не найдено",
                    value = "{\n  \"message\": \"Media not found with id: 1\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Внутренняя ошибка",
                    value = "{\n  \"message\": \"Internal server error\",\n  \"status\": 500,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable Long id) {
        log.debug("Request to get media by id: {}", id);
        MediaResponse result = mediaService.getMediaById(id);
        log.info("Successfully retrieved media with id: {}", id);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Создание нового медиа-данного",
        description = "Создает новое медиа-данное. Загружает файл в MinIO и сохраняет метаданные в базе данных.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для создания медиа-данного (multipart/form-data)",
        required = true,
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = MediaRequest.class),
            examples = @ExampleObject(
                name = "Пример запроса",
                value = "{\n  \"note\": \"Фото прогресса\",\n  \"file\": \"(binary)\"\n}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Медиа-данные успешно созданы",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MediaResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"presignedUrl\": \"http://localhost:9000/test-bucket/uuid-generated-path.jpg?signature=abc123\",\n  \"note\": \"Фото прогресса\",\n  \"fileSize\": 1024,\n  \"mimeType\": \"image/jpeg\",\n  \"createdAt\": \"2024-12-15T10:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных или файл пустой",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Ошибка валидации",
                    value = "{\n  \"message\": \"File is empty\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Не авторизован",
                    value = "{\n  \"message\": \"Unauthorized\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Пользователь не найден",
                    value = "{\n  \"message\": \"User not found with id: 1\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка загрузки файла",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Внутренняя ошибка",
                    value = "{\n  \"message\": \"Failed to upload file to MinIO\",\n  \"status\": 500,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> createMedia(
        @RequestPart(value = "note", required = false) String note,
        @RequestPart("file") @NotNull(message = "File is required") MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }
        
        if (note != null && note.length() > 500) {
            throw new IllegalArgumentException("Note cannot exceed 500 characters");
        }
        
        MediaRequest request = new MediaRequest(note);
        log.debug("Request to create media: filename={}, size={}, contentType={}, note={}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType(), note);
        MediaResponse result = mediaService.createMedia(request, file);
        log.info("Successfully created media with id: {}, filename: {}", 
            result.id(), file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
        summary = "Удаление медиа-данного по ID",
        description = "Удаляет медиа-данное по ID. Можно удалять только свои медиа-данные. Файл также удаляется из MinIO.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID медиа-данных для удаления", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Медиа-данные успешно удалены",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Не авторизован",
                    value = "{\n  \"message\": \"Unauthorized\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещен. Можно удалять только свои медиа-данные",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Доступ запрещен",
                    value = "{\n  \"message\": \"You can only delete your own media\",\n  \"status\": 403,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Медиа-данные не найдены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Медиа не найдено",
                    value = "{\n  \"message\": \"Media not found with id: 1\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка удаления файла",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Внутренняя ошибка",
                    value = "{\n  \"message\": \"Failed to delete file from MinIO\",\n  \"status\": 500,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        log.debug("Request to delete media with id: {}", id);
        mediaService.deleteMedia(id);
        log.info("Successfully deleted media with id: {}", id);
        return ResponseEntity.noContent().build();
    }

}
