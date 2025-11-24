package org.example.fitnesstracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.dto.response.analytics.MaxAchievements;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.repository.WorkoutsRepository;
import org.example.fitnesstracker.repository.specification.WorkoutSpecifications;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final WorkoutsRepository workoutsRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(LocalDate dateFrom, LocalDate dateTo) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Getting analytics for user {} from {} to {}", currentUserId, dateFrom, dateTo);

        
        Specification<Workout> allWorkoutsSpec = WorkoutSpecifications.belongsToUser(currentUserId);
        List<Workout> allWorkouts = workoutsRepository.findAll(allWorkoutsSpec);

        
        List<Workout> workoutsInPeriod;
        if (dateFrom != null || dateTo != null) {
            Specification<Workout> periodSpec = WorkoutSpecifications.belongsToUser(currentUserId)
                .and(WorkoutSpecifications.hasDateFrom(dateFrom))
                .and(WorkoutSpecifications.hasDateTo(dateTo));
            workoutsInPeriod = workoutsRepository.findAll(periodSpec);
        } else {
            workoutsInPeriod = allWorkouts;
        }

        
        long totalWorkouts = allWorkouts.size();
        double totalWeightLifted = calculateTotalWeightLifted(allWorkouts);
        int totalCaloriesBurned = allWorkouts.stream()
            .filter(w -> w.getCalories() != null)
            .mapToInt(Workout::getCalories)
            .sum();
        int totalDuration = allWorkouts.stream()
            .filter(w -> w.getDuration() != null)
            .mapToInt(Workout::getDuration)
            .sum();

        
        long workoutsInPeriodCount = workoutsInPeriod.size();
        double totalWeightLiftedInPeriod = calculateTotalWeightLifted(workoutsInPeriod);
        int totalCaloriesBurnedInPeriod = workoutsInPeriod.stream()
            .filter(w -> w.getCalories() != null)
            .mapToInt(Workout::getCalories)
            .sum();
        int totalDurationInPeriod = workoutsInPeriod.stream()
            .filter(w -> w.getDuration() != null)
            .mapToInt(Workout::getDuration)
            .sum();

        log.info("Analytics calculated: total workouts: {}, workouts in period: {}, total weight: {} kg, total calories: {}",
            totalWorkouts, workoutsInPeriodCount, totalWeightLifted, totalCaloriesBurned);

        Map<WorkoutType, Integer> workoutsByType = calculateWorkoutsByType(allWorkouts);
        MaxAchievements maxAchievements = calculateMaxAchievements(allWorkouts);

        return new AnalyticsResponse(
            totalWorkouts,
            workoutsInPeriodCount,
            totalWeightLifted,
            totalWeightLiftedInPeriod,
            totalCaloriesBurned,
            totalCaloriesBurnedInPeriod,
            totalDuration,
            totalDurationInPeriod,
            dateFrom,
            dateTo,
            workoutsByType,
            maxAchievements
        );
    }

    private double calculateTotalWeightLifted(List<Workout> workouts) {
        double totalWeight = 0.0;
        for (Workout workout : workouts) {
            if (workout.getWorkoutExercises() != null) {
                for (WorkoutExercise exercise : workout.getWorkoutExercises()) {
                    if (exercise.getWeight() != null && exercise.getReps() != null && exercise.getSets() != null) {
                        
                        totalWeight += exercise.getWeight() * exercise.getReps() * exercise.getSets();
                    }
                }
            }
        }
        return totalWeight;
    }
    private Map<WorkoutType, Integer> calculateWorkoutsByType(List<Workout> workouts) {
        return workouts.stream()
            .collect(Collectors.groupingBy(Workout::getType, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private MaxAchievements calculateMaxAchievements(List<Workout> workouts) {
        return new MaxAchievements(
            calculateMaxWeightByExercise(workouts),
            calculateMaxDistanceByExercise(workouts),
            calculateMaxTimeByExercise(workouts),
            calculateMaxCaloriesBurnedInWorkout(workouts),
            calculateMaxDurationInWorkout(workouts)
        );
    }
    private Map<String, Integer> calculateMaxWeightByExercise(List<Workout> workouts) {
        Map<String, Integer> maxWeights = new HashMap<>();
        
        for (Workout workout : workouts) {
            if (workout.getWorkoutExercises() == null) {
                continue;
            }
            
            for (WorkoutExercise workoutExercise : workout.getWorkoutExercises()) {
                if (workoutExercise.getWeight() == null) {
                    continue;
                }
                
                String exerciseName = workoutExercise.getExercise().getName();
                int weight = workoutExercise.getWeight().intValue();
                
                if (!maxWeights.containsKey(exerciseName) || weight > maxWeights.get(exerciseName)) {
                    maxWeights.put(exerciseName, weight);
                }
            }
        }
        
        return maxWeights;
    }
    
    private Map<String, Integer> calculateMaxDistanceByExercise(List<Workout> workouts) {
        Map<String, Integer> maxDistances = new HashMap<>();
        
        for (Workout workout : workouts) {
            if (workout.getWorkoutExercises() == null) {
                continue;
            }
            
            for (WorkoutExercise workoutExercise : workout.getWorkoutExercises()) {
                if ( workoutExercise.getDistance() == null) {
                    continue;
                }
                
                String exerciseName = workoutExercise.getExercise().getName();
                int distance = workoutExercise.getDistance().intValue();
                
                if (!maxDistances.containsKey(exerciseName) || distance > maxDistances.get(exerciseName)) {
                    maxDistances.put(exerciseName, distance);
                }
            }
        }
        
        return maxDistances;
    }
    
    private Map<String, Integer> calculateMaxTimeByExercise(List<Workout> workouts) {
        Map<String, Integer> maxTimes = new HashMap<>();
        
        for (Workout workout : workouts) {
            if (workout.getWorkoutExercises() == null) {
                continue;
            }
            
            for (WorkoutExercise workoutExercise : workout.getWorkoutExercises()) {
                if (workoutExercise.getExercise() == null || workoutExercise.getTime() == null) {
                    continue;
                }
                
                String exerciseName = workoutExercise.getExercise().getName();
                int time = workoutExercise.getTime();
                
                if (!maxTimes.containsKey(exerciseName) || time > maxTimes.get(exerciseName)) {
                    maxTimes.put(exerciseName, time);
                }
            }
        }
        
        return maxTimes;
    }
    
    private Integer calculateMaxCaloriesBurnedInWorkout(List<Workout> workouts) {
        return workouts.stream()
            .map(Workout::getCalories)
            .max(Integer::compare)
            .orElse(0);
    }
    private Integer calculateMaxDurationInWorkout(List<Workout> workouts) {
        return workouts.stream()
            .map(Workout::getDuration)
            .max(Integer::compare)
            .orElse(0);
    }
    
}

