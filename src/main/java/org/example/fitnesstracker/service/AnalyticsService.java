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

import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
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
    public AnalyticsResponse getAnalytics(AnalyticsRequest request) {
        LocalDate dateFrom = request.dateFilter() != null ? request.dateFilter().dateFrom() : null;
        LocalDate dateTo = request.dateFilter() != null ? request.dateFilter().dateTo() : null;

        List<Workout> workouts = getWorkoutsInPeriod(request);

        long totalWorkouts = workouts.size();
        double totalWeightLifted = calculateTotalWeightLifted(workouts);
        int totalCaloriesBurned = calculateTotalCaloriesBurned(workouts);
        int totalDuration = calculateTotalDuration(workouts);

        log.info("Analytics calculated: workouts: {}, total weight: {} kg, total calories: {}",
            totalWorkouts, totalWeightLifted, totalCaloriesBurned);

        Map<WorkoutType, Integer> workoutsByType = calculateWorkoutsByType(workouts);
        MaxAchievements maxAchievements = calculateMaxAchievements(workouts);

        return new AnalyticsResponse(
            totalWorkouts,
            totalWeightLifted,
            totalCaloriesBurned,
            totalDuration,
            dateFrom,
            dateTo,
            workoutsByType,
            maxAchievements
        );
    }

    private List<Workout> getWorkoutsInPeriod(AnalyticsRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Workout> periodSpec = WorkoutSpecifications.belongsToUser(currentUserId);
        if (request != null && request.dateFilter() != null) {
            periodSpec = periodSpec
                .and(WorkoutSpecifications.hasDateFrom(request.dateFilter().dateFrom()))
                .and(WorkoutSpecifications.hasDateTo(request.dateFilter().dateTo()));
        }
        return workoutsRepository.findAll(periodSpec);
    }

    private int calculateTotalCaloriesBurned(List<Workout> workouts) {
        return workouts.stream()
            .filter(w -> w.getCalories() != null)
            .mapToInt(Workout::getCalories)
            .sum();
    }

    private int calculateTotalDuration(List<Workout> workouts) {
        return workouts.stream()
            .filter(w -> w.getDuration() != null)
            .mapToInt(Workout::getDuration)
            .sum();
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
                if (workoutExercise.getDistance() == null) {
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

    private int calculateMaxCaloriesBurnedInWorkout(List<Workout> workouts) {
        return workouts.stream()
            .filter(w -> w.getCalories() != null)
            .map(Workout::getCalories)
            .max(Integer::compare)
            .orElse(0);
    }

    private int calculateMaxDurationInWorkout(List<Workout> workouts) {
        return workouts.stream()
            .filter(w -> w.getDuration() != null)
            .map(Workout::getDuration)
            .max(Integer::compare)
            .orElse(0);
    }
}

