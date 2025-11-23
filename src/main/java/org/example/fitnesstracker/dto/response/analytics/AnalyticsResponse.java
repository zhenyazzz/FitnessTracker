package org.example.fitnesstracker.dto.response.analytics;

import java.time.LocalDate;
import java.util.Map;

import org.example.fitnesstracker.model.enums.WorkoutType;

public record AnalyticsResponse(
    Long totalWorkouts,
    Long workoutsInPeriod,
    Double totalWeightLifted,
    Double totalWeightLiftedInPeriod,
    Integer totalCaloriesBurned,
    Integer totalCaloriesBurnedInPeriod,
    Integer totalDuration,
    Integer totalDurationInPeriod,
    LocalDate periodStart,
    LocalDate periodEnd,

    Map<WorkoutType, Integer> workoutsByType,
    MaxAchievements maxAchievements
) {
}

