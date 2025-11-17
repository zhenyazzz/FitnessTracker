package org.example.fitnesstracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitnesstracker.dto.request.auth.LoginRequest;
import org.example.fitnesstracker.dto.request.auth.LogoutRequest;
import org.example.fitnesstracker.dto.request.auth.RefreshTokenRequest;
import org.example.fitnesstracker.dto.request.auth.RegisterRequest;
import org.example.fitnesstracker.dto.response.AuthResponse;
import org.example.fitnesstracker.dto.response.ErrorResponse;
import org.example.fitnesstracker.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и авторизации пользователей")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с ролью USER и возвращает JWT токены (access token и refresh token)"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для регистрации пользователя",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "Пример запроса",
                            value = "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\",\n  \"username\": \"john_doe\"\n}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешный ответ",
                                    value = "{\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = "{\n  \"message\": \"email: Email must be valid, password: Password must be at least 4 characters\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Конфликт email",
                                    value = "{\n  \"message\": \"Email already exists: user@example.com\",\n  \"status\": 409,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по email и паролю, возвращает JWT токены (access token и refresh token)"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для входа в систему",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "Пример запроса",
                            value = "{\n  \"email\": \"user@example.com\",\n  \"password\": \"password123\"\n}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный вход",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешный ответ",
                                    value = "{\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = "{\n  \"message\": \"email: Email must be valid, password: Password must be at least 4 characters\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный email или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Неверные учетные данные",
                                    value = "{\n  \"message\": \"Invalid password\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
                                    value = "{\n  \"message\": \"User with email user@example.com not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Обновление токена доступа",
            description = "Обновляет access token и refresh token используя текущий refresh token"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token для обновления",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(
                            name = "Пример запроса",
                            value = "{\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены успешно обновлены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Успешный ответ",
                                    value = "{\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = "{\n  \"message\": \"refreshToken: Refresh token is required\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token истек или недействителен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Токен истек",
                                    value = "{\n  \"message\": \"Refresh token expired. Please login again.\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Refresh token не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Токен не найден",
                                    value = "{\n  \"message\": \"Refresh token not found. Please login again.\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Выход из системы",
            description = "Удаляет refresh token, завершая сессию пользователя"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token для выхода",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LogoutRequest.class),
                    examples = @ExampleObject(
                            name = "Пример запроса",
                            value = "{\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Выход выполнен успешно",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Ошибка валидации",
                                    value = "{\n  \"message\": \"refreshToken: Refresh token is required\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token истек или недействителен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Токен истек",
                                    value = "{\n  \"message\": \"Refresh token expired. Please login again.\",\n  \"status\": 401,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Refresh token не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Токен не найден",
                                    value = "{\n  \"message\": \"Refresh token eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

}
