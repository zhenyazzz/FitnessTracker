package org.example.fitnesstracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.dto.response.analytics.MaxAchievements;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.repository.WorkoutsRepository;

import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
import java.time.LocalDate;

import org.example.fitnesstracker.security.SecurityUtils;

import java.util.Map;

@Service()
@RequiredArgsConstructor
public class AnalyticsService {

    private final WorkoutsRepository workoutsRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(AnalyticsRequest request) {
        LocalDate dateFrom = request.dateFilter() != null ? request.dateFilter().dateFrom() : null;
        LocalDate dateTo = request.dateFilter() != null ? request.dateFilter().dateTo() : null;
        
        Long userId = SecurityUtils.getCurrentUserId();

        long totalWorkouts = workoutsRepository.calculateTotalWorkoutsByUserId(userId, dateFrom, dateTo);

        double totalWeightLifted = workoutsRepository.calculateTotalWeightLiftedByUserId(userId, dateFrom, dateTo);

        int totalCaloriesBurned = workoutsRepository.calculateTotalCaloriesBurnedByUserId(userId, dateFrom, dateTo);

        int totalDuration = workoutsRepository.calculateTotalDurationByUserId(userId, dateFrom, dateTo);


        Map<WorkoutType, Integer> workoutsByType = workoutsRepository.findWorkoutsByType(userId, dateFrom, dateTo);
        MaxAchievements maxAchievements = calculateMaxAchievements(userId, dateFrom, dateTo);

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

    private MaxAchievements calculateMaxAchievements(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return new MaxAchievements(
            workoutsRepository.findMaxWeightByExercise(userId, dateFrom, dateTo),
            workoutsRepository.findMaxDistanceByExercise(userId, dateFrom, dateTo),
            workoutsRepository.findMaxTimeByExercise(userId, dateFrom, dateTo),
            workoutsRepository.calculateMaxCaloriesBurnedInWorkout(userId, dateFrom, dateTo),
            workoutsRepository.calculateMaxDurationInWorkout(userId, dateFrom, dateTo)
        );
    }

}

