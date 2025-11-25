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
import jakarta.validation.Valid;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.response.ErrorResponse;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@Tag(name = "Workouts", description = "API для работы с тренировками")
public interface WorkoutsControllerApi {

    @Operation(
        summary = "Получение списка всех тренировок",
        description = "Получает список всех тренировок пользователя с возможностью фильтрации по типу, дате, длительности, калориям и пагинации. " +
                      "По умолчанию сортировка по дате (date) в порядке убывания. Все фильтры опциональны.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Фильтры для получения тренировок (опционально)",
        required = false,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = WorkoutFilterDto.class),
            examples = @ExampleObject(
                name = "Пример фильтра",
                value = "{\n  \"type\": \"STRENGTH\",\n  \"dateFilter\": {\n    \"dateFrom\": \"2024-12-01\",\n    \"dateTo\": \"2024-12-31\"\n  },\n  \"durationFilter\": {\n    \"durationFrom\": 30,\n    \"durationTo\": 90\n  },\n  \"caloriesFilter\": {\n    \"caloriesFrom\": 200,\n    \"caloriesTo\": 500\n  }\n}"
            )
        )
    )
    @Parameters({
        @Parameter(
            name = "page",
            description = "Номер страницы (начиная с 0)",
            example = "0"
        ),
        @Parameter(
            name = "size",
            description = "Размер страницы (по умолчанию 10, максимум 100)",
            example = "10"
        ),
        @Parameter(
            name = "sort",
            description = "Параметры сортировки (по умолчанию: date,desc). Формат: поле,направление. Доступные поля: date, duration, calories, createdAt",
            example = "date,desc"
        )
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список тренировок успешно получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных (например, dateFrom > dateTo, durationFrom > durationTo, caloriesFrom > caloriesTo)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Ошибка валидации",
                    value = "{\n  \"message\": \"dateFrom must be before or equal to dateTo\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<Page<WorkoutResponse>> getAllWorkouts(
        @Valid @RequestBody WorkoutFilterDto filter,
        @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(
        summary = "Получение тренировки по ID",
        description = "Получает тренировку по ID",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID тренировки", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Тренировка успешно получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"name\": \"Утренняя тренировка\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2024-12-15\",\n  \"duration\": 60,\n  \"calories\": 350,\n  \"createdAt\": \"2024-12-15T10:00:00\",\n  \"exercises\": [\n    {\n      \"id\": 1,\n      \"exerciseId\": 1,\n      \"exerciseName\": \"Жим лежа\",\n      \"muscleGroup\": \"CHEST\",\n      \"sets\": 4,\n      \"reps\": 12,\n      \"weight\": 80.0,\n      \"distance\": null,\n      \"time\": null\n    }\n  ]\n}"
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
            description = "Тренировка не найдена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Тренировка не найдена",
                    value = "{\n  \"message\": \"Workout with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable Long id);

    @Operation(
        summary = "Создание новой тренировки",
        description = "Создает новую тренировку",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для создания тренировки",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CreateWorkoutRequest.class),
            examples = @ExampleObject(name = "Пример запроса", value = "{\n  \"name\": \"Тренировка 1\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2025-01-01\",\n  \"duration\": 30,\n  \"calories\": 100,\n  \"exercises\": [\n    {\n      \"exerciseId\": 1,\n      \"sets\": 4,\n      \"reps\": 12,\n      \"weight\": 80.0,\n      \"distance\": null,\n      \"time\": null\n    }\n  ]\n}")
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Тренировка успешно создана",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"name\": \"Утренняя тренировка\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2024-12-15\",\n  \"duration\": 60,\n  \"calories\": 350,\n  \"createdAt\": \"2024-12-15T10:00:00\",\n  \"exercises\": [\n    {\n      \"id\": 1,\n      \"exerciseId\": 1,\n      \"exerciseName\": \"Жим лежа\",\n      \"muscleGroup\": \"CHEST\",\n      \"sets\": 4,\n      \"reps\": 12,\n      \"weight\": 80.0,\n      \"distance\": null,\n      \"time\": null\n    }\n  ]\n}"
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
                    value = "{\n  \"message\": \"name: Workout name cannot be empty, duration: Duration must be at least 1 minute\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
            description = "Упражнение или пользователь не найден",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Упражнение не найдено",
                    value = "{\n  \"message\": \"Exercise with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody CreateWorkoutRequest request);

    @Operation(
        summary = "Обновление тренировки по ID",
        description = "Обновляет основные поля тренировки по ID (название, тип, дата, длительность, калории). Все поля опциональны. Упражнения не обновляются через этот метод.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID тренировки", required = true, example = "1")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для обновления тренировки (все поля опциональны)",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UpdateWorkoutRequest.class),
            examples = @ExampleObject(
                name = "Пример запроса",
                value = "{\n  \"name\": \"Обновленная тренировка\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2024-12-15\",\n  \"duration\": 75,\n  \"calories\": 450\n}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Тренировка успешно обновлена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"name\": \"Обновленная тренировка\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2024-12-15\",\n  \"duration\": 75,\n  \"calories\": 450,\n  \"createdAt\": \"2024-12-15T10:00:00\",\n  \"exercises\": [\n    {\n      \"id\": 1,\n      \"exerciseId\": 1,\n      \"exerciseName\": \"Жим лежа\",\n      \"muscleGroup\": \"CHEST\",\n      \"sets\": 5,\n      \"reps\": 10,\n      \"weight\": 85.0,\n      \"distance\": null,\n      \"time\": null\n    }\n  ]\n}"
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
                    value = "{\n  \"message\": \"duration: Duration must be at least 1 minute\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
            responseCode = "403",
            description = "Доступ запрещен. Можно обновлять только свои тренировки",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Доступ запрещен",
                    value = "{\n  \"message\": \"You can only update your own workouts\",\n  \"status\": 403,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Тренировка не найдена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Тренировка не найдена",
                    value = "{\n  \"message\": \"Workout with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable Long id, @Valid @RequestBody UpdateWorkoutRequest request);

    @Operation(
        summary = "Удаление тренировки по ID",
        description = "Удаляет тренировку по ID",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID тренировки", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Тренировка успешно удалена",
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
            description = "Доступ запрещен. Можно удалять только свои тренировки",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Доступ запрещен",
                    value = "{\n  \"message\": \"You can only delete your own workouts\",\n  \"status\": 403,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Тренировка не найдена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Тренировка не найдена",
                    value = "{\n  \"message\": \"Workout with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<Void> deleteWorkout(@PathVariable Long id);

    @Operation(
        summary = "Добавление упражнения в тренировку",
        description = "Добавляет новое упражнение в существующую тренировку. Упражнение должно существовать в системе.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameter(name = "id", description = "ID тренировки", required = true, example = "1")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для добавления упражнения в тренировку",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CreateWorkoutExerciseRequest.class),
            examples = @ExampleObject(
                name = "Пример запроса",
                value = "{\n  \"exerciseId\": 1,\n  \"sets\": 4,\n  \"reps\": 12,\n  \"weight\": 80.0,\n  \"distance\": null,\n  \"time\": null\n}"
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Упражнение успешно добавлено в тренировку",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutResponse.class),
                examples = @ExampleObject(
                    name = "Успешный ответ",
                    value = "{\n  \"id\": 1,\n  \"name\": \"Утренняя тренировка\",\n  \"type\": \"STRENGTH\",\n  \"date\": \"2024-12-15\",\n  \"duration\": 60,\n  \"calories\": 350,\n  \"createdAt\": \"2024-12-15T10:00:00\",\n  \"exercises\": [\n    {\n      \"id\": 1,\n      \"exerciseId\": 1,\n      \"exerciseName\": \"Жим лежа\",\n      \"muscleGroup\": \"CHEST\",\n      \"sets\": 4,\n      \"reps\": 12,\n      \"weight\": 80.0,\n      \"distance\": null,\n      \"time\": null\n    },\n    {\n      \"id\": 2,\n      \"exerciseId\": 2,\n      \"exerciseName\": \"Приседания\",\n      \"muscleGroup\": \"LEGS\",\n      \"sets\": 3,\n      \"reps\": 15,\n      \"weight\": 100.0,\n      \"distance\": null,\n      \"time\": null\n    }\n  ]\n}"
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
                    value = "{\n  \"message\": \"sets: Sets must be at least 1, reps: Reps must be at least 1\",\n  \"status\": 400,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
            responseCode = "403",
            description = "Доступ запрещен. Можно добавлять упражнения только в свои тренировки",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Доступ запрещен",
                    value = "{\n  \"message\": \"You can only modify your own workouts\",\n  \"status\": 403,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Тренировка или упражнение не найдены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Тренировка не найдена",
                    value = "{\n  \"message\": \"Workout with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<WorkoutResponse> addExerciseToWorkout(@PathVariable Long id, @Valid @RequestBody CreateWorkoutExerciseRequest request);

    @Operation(
        summary = "Удаление упражнения из тренировки",
        description = "Удаляет упражнение из тренировки по ID упражнения в тренировке (workoutExerciseId).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Parameters({
        @Parameter(name = "id", description = "ID тренировки", required = true, example = "1"),
        @Parameter(name = "exerciseId", description = "ID упражнения в тренировке (workoutExerciseId)", required = true, example = "1")
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Упражнение успешно удалено из тренировки",
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
            description = "Доступ запрещен. Можно удалять упражнения только из своих тренировок",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Доступ запрещен",
                    value = "{\n  \"message\": \"You can only modify your own workouts\",\n  \"status\": 403,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Тренировка или упражнение не найдены",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Упражнение не найдено",
                    value = "{\n  \"message\": \"Workout exercise with id 1 not found\",\n  \"status\": 404,\n  \"timestamp\": \"2025-11-17T18:00:00\"\n}"
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
    ResponseEntity<Void> removeExerciseFromWorkout(@PathVariable Long id, @PathVariable Long exerciseId);
}
