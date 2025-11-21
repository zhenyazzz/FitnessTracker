package org.example.fitnesstracker.controller;

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
import lombok.RequiredArgsConstructor;

import org.example.fitnesstracker.dto.response.ErrorResponse;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "API для аналитики тренировок")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
        summary = "Получение аналитики тренировок",
        description = "Возвращает статистику по тренировкам: общее количество тренировок, поднятый вес, сожженные калории, длительность. " +
                      "Можно указать период для получения статистики за определенный промежуток времени.",
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
                            "  \"totalWorkouts\": 25,\n" +
                            "  \"workoutsInPeriod\": 10,\n" +
                            "  \"totalWeightLifted\": 12500.5,\n" +
                            "  \"totalWeightLiftedInPeriod\": 4500.0,\n" +
                            "  \"totalCaloriesBurned\": 8500,\n" +
                            "  \"totalCaloriesBurnedInPeriod\": 3200,\n" +
                            "  \"totalDuration\": 1500,\n" +
                            "  \"totalDurationInPeriod\": 600,\n" +
                            "  \"periodStart\": \"2024-12-01\",\n" +
                            "  \"periodEnd\": \"2024-12-31\",\n" +
                            "  \"workoutsByType\": {\n" +
                            "    \"STRENGTH\": 15,\n" +
                            "    \"CARDIO\": 8,\n" +
                            "    \"YOGA\": 2\n" +
                            "  },\n" +
                            "  \"maxAchievements\": {\n" +
                            "    \"maxWeightByExercise\": {\n" +
                            "      \"Жим лежа\": 100,\n" +
                            "      \"Приседания\": 120,\n" +
                            "      \"Становая тяга\": 150\n" +
                            "    },\n" +
                            "    \"maxDistanceByExercise\": {\n" +
                            "      \"Бег\": 10,\n" +
                            "      \"Велосипед\": 25\n" +
                            "    },\n" +
                            "    \"maxTimeByExercise\": {\n" +
                            "      \"Плавание\": 3600,\n" +
                            "      \"Бег\": 1800\n" +
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
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo
    ) {
        
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }

        return ResponseEntity.ok(analyticsService.getAnalytics(dateFrom, dateTo));
    }
}
