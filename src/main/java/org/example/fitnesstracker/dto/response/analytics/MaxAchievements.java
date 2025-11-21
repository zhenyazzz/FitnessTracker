package org.example.fitnesstracker.dto.response.analytics;

import java.util.Map;

public record MaxAchievements(
    Map<String, Integer> maxWeightByExercise,
    Map<String, Integer> maxDistanceByExercise,
    Map<String, Integer> maxTimeByExercise,
    Integer maxCaloriesBurnedInWorkout,
    Integer maxDurationInWorkout
) {

}
