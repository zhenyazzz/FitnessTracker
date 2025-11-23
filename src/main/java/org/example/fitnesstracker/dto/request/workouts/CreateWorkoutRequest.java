package org.example.fitnesstracker.dto.request.workouts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

import org.example.fitnesstracker.model.enums.WorkoutType;

public record CreateWorkoutRequest(
    @NotBlank(message = "Workout name cannot be empty")
    @Size(max = 100, message = "Workout name cannot exceed 100 characters")
    String name,
    
    @NotNull(message = "Workout type is required")
    WorkoutType type,
    
    @NotNull(message = "Workout date is required")
    @PastOrPresent(message = "Workout date cannot be in the future")
    LocalDate date,
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Integer duration, // minutes
    
    @Min(value = 1, message = "Calories must be greater than 0")
    Integer calories,
    
    @Valid
    List<CreateWorkoutExerciseRequest> exercises 
) {
}
