package org.example.fitnesstracker.dto.response.workouts;

import org.example.fitnesstracker.model.MuscleGroup;

public record WorkoutExerciseResponse(
    Long id,
    Long exerciseId,
    String exerciseName,
    MuscleGroup muscleGroup,
    Integer sets,
    Integer reps,
    Double weight,
    Double distance,
    Integer time // seconds
) {
}
