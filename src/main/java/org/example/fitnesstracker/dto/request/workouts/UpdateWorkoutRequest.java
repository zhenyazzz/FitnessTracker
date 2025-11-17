package org.example.fitnesstracker.dto.request.workouts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

import org.example.fitnesstracker.model.enums.WorkoutType;

public record UpdateWorkoutRequest(
    @Size(max = 100, message = "Workout name cannot exceed 100 characters")
    String name,
    
    WorkoutType type,
    
    @PastOrPresent(message = "Workout date cannot be in the future")
    LocalDate date,
    
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Integer duration, // minutes
    
    @Min(value = 0, message = "Calories cannot be negative")
    Integer calories,
    
    @Valid
    List<UpdateWorkoutExerciseRequest> exercises
) {
}
