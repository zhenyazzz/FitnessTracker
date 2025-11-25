package org.example.fitnesstracker.dto.request.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;

public record UpdateWorkoutExerciseRequest(

    Long existingExerciseId,
    
    Long newExerciseId,
    
    @Min(value = 1, message = "Sets must be at least 1")
    Integer sets,
    
    @Min(value = 1, message = "Reps must be at least 1")
    Integer reps,
    
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    Double weight,
    
    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    Double distance,
    
    @Min(value = 0, message = "Time cannot be negative")
    Integer time 
) {

}
