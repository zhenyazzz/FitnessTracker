package org.example.fitnesstracker.dto.response.analytics;

import java.time.LocalDate;
import java.util.Map;

import org.example.fitnesstracker.model.enums.WorkoutType;

public record AnalyticsResponse(
    Long totalWorkouts,
    Double totalWeightLifted,
    Integer totalCaloriesBurned,
    Integer totalDuration,
    LocalDate periodStart,
    LocalDate periodEnd,

    Map<WorkoutType, Integer> workoutsByType,
    MaxAchievements maxAchievements
) {
}

