package org.example.fitnesstracker.dto.response.workouts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.fitnesstracker.model.enums.WorkoutType;

public record WorkoutResponse(
    Long id,
    String name,
    WorkoutType type,
    LocalDate date,
    Integer duration, // minutes
    Integer calories,
    LocalDateTime createdAt,
    List<WorkoutExerciseResponse> exercises
) {
}
