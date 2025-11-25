package org.example.fitnesstracker.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;

import org.example.fitnesstracker.dto.response.ErrorResponse;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;

@Tag(name = "Analytics", description = "API для аналитики тренировок")
public interface AnalyticsControllerApi {
    
    @Operation(
        summary = "Получение аналитики тренировок",
        description = "Возвращает статистику по тренировкам: количество тренировок, поднятый вес, сожженные калории, длительность. " +
                      "Можно указать период для получения статистики за определенный промежуток времени. " +
                      "Если период не указан, анализируются все тренировки пользователя.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameters({
        @Parameter(
            name = "dateFrom",
            description = "Начальная дата периода (формат: YYYY-MM-DD). Если не указана, анализируются все тренировки",
            example = "2024-12-01"
        ),
        @Parameter(
            name = "dateTo",
            description = "Конечная дата периода (формат: YYYY-MM-DD). Если не указана, анализируются все тренировки",
            example = "2024-12-31"
        )
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Аналитика успешно получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AnalyticsResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n" +
                            "  \"totalWorkouts\": 10,\n" +
                            "  \"totalWeightLifted\": 4500.0,\n" +
                            "  \"totalCaloriesBurned\": 3200,\n" +
                            "  \"totalDuration\": 600,\n" +
                            "  \"periodStart\": \"2024-12-01\",\n" +
                            "  \"periodEnd\": \"2024-12-31\",\n" +
                            "  \"workoutsByType\": {\n" +
                            "    \"STRENGTH\": 6,\n" +
                            "    \"CARDIO\": 3,\n" +
                            "    \"YOGA\": 1\n" +
                            "  },\n" +
                            "  \"maxAchievements\": {\n" +
                            "    \"maxWeightByExercise\": {\n" +
                            "      \"Bench Press\": 100,\n" +
                            "      \"Squat\": 120,\n" +
                            "      \"Deadlift\": 150\n" +
                            "    },\n" +
                            "    \"maxDistanceByExercise\": {\n" +
                            "      \"Running\": 10,\n" +
                            "      \"Burpees\": 25\n" +
                            "    },\n" +
                            "    \"maxTimeByExercise\": {\n" +
                            "      \"Mountain Climbers\": 3600,\n" +
                            "      \"Running\": 1800\n" +
                            "    },\n" +
                            "    \"maxCaloriesBurnedInWorkout\": 500,\n" +
                            "    \"maxDurationInWorkout\": 90\n" +
                            "  }\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных (например, dateFrom > dateTo)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Ошибка валидации",
                    value = "{\n" +
                            "  \"message\": \"dateFrom cannot be after dateTo\",\n" +
                            "  \"status\": 400,\n" +
                            "  \"timestamp\": \"2025-11-17T18:00:00\"\n" +
                            "}"
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
                    value = "{\n" +
                            "  \"message\": \"Unauthorized\",\n" +
                            "  \"status\": 401,\n" +
                            "  \"timestamp\": \"2025-11-17T18:00:00\"\n" +
                            "}"
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
                    value = "{\n" +
                            "  \"message\": \"Internal server error\",\n" +
                            "  \"status\": 500,\n" +
                            "  \"timestamp\": \"2025-11-17T18:00:00\"\n" +
                            "}"
                )
            )
        )
    })
    ResponseEntity<AnalyticsResponse> getAnalytics(
        @Valid @RequestBody AnalyticsRequest request
    );

}
