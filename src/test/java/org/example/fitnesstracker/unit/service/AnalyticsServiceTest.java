package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.dto.request.DateFilterDto;
import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.dto.response.analytics.MaxAchievements;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.repository.WorkoutsRepository;
import org.example.fitnesstracker.security.SecurityUtils;
import org.example.fitnesstracker.service.AnalyticsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Test")
class AnalyticsServiceTest {

    @Mock
    private WorkoutsRepository workoutsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Long userId;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        userId = 1L;
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    @DisplayName("Возвращает корректные агрегаты без фильтра дат")
    void shouldReturnAggregatedAnalyticsWithoutDateFilter() {
        AnalyticsRequest request = new AnalyticsRequest(null);

        Map<WorkoutType, Integer> workoutsByType = Map.of(
            WorkoutType.STRENGTH, 3,
            WorkoutType.CARDIO, 2
        );

        Map<String, Integer> maxWeight = Map.of("Bench Press", 100);
        Map<String, Integer> maxDistance = Map.of("Run", 5000);
        Map<String, Integer> maxTime = Map.of("Run", 1800);

        when(workoutsRepository.calculateTotalWorkoutsByUserId(userId, null, null)).thenReturn(5L);
        when(workoutsRepository.calculateTotalWeightLiftedByUserId(userId, null, null)).thenReturn(3500.0);
        when(workoutsRepository.calculateTotalCaloriesBurnedByUserId(userId, null, null)).thenReturn(1200);
        when(workoutsRepository.calculateTotalDurationByUserId(userId, null, null)).thenReturn(240);
        when(workoutsRepository.findWorkoutsByType(userId, null, null)).thenReturn(workoutsByType);
        when(workoutsRepository.findMaxWeightByExercise(userId, null, null)).thenReturn(maxWeight);
        when(workoutsRepository.findMaxDistanceByExercise(userId, null, null)).thenReturn(maxDistance);
        when(workoutsRepository.findMaxTimeByExercise(userId, null, null)).thenReturn(maxTime);
        when(workoutsRepository.calculateMaxCaloriesBurnedInWorkout(userId, null, null)).thenReturn(600);
        when(workoutsRepository.calculateMaxDurationInWorkout(userId, null, null)).thenReturn(90);

        AnalyticsResponse response = analyticsService.getAnalytics(request);

        assertNotNull(response);
        assertEquals(5L, response.totalWorkouts());
        assertEquals(3500.0, response.totalWeightLifted());
        assertEquals(1200, response.totalCaloriesBurned());
        assertEquals(240, response.totalDuration());
        assertEquals(workoutsByType, response.workoutsByType());

        MaxAchievements maxAchievements = response.maxAchievements();
        assertNotNull(maxAchievements);
        assertEquals(maxWeight, maxAchievements.maxWeightByExercise());
        assertEquals(maxDistance, maxAchievements.maxDistanceByExercise());
        assertEquals(maxTime, maxAchievements.maxTimeByExercise());
        assertEquals(600, maxAchievements.maxCaloriesBurnedInWorkout());
        assertEquals(90, maxAchievements.maxDurationInWorkout());
    }

    @Test
    @DisplayName("Корректно обрабатывает пустые данные с фильтром дат")
    void shouldHandleEmptyAnalyticsWithDateFilter() {
        LocalDate dateFrom = LocalDate.of(2024, 1, 1);
        LocalDate dateTo = LocalDate.of(2024, 12, 31);
        AnalyticsRequest request = new AnalyticsRequest(new DateFilterDto(dateFrom, dateTo));

        when(workoutsRepository.calculateTotalWorkoutsByUserId(userId, dateFrom, dateTo)).thenReturn(0L);
        when(workoutsRepository.calculateTotalWeightLiftedByUserId(userId, dateFrom, dateTo)).thenReturn(0.0);
        when(workoutsRepository.calculateTotalCaloriesBurnedByUserId(userId, dateFrom, dateTo)).thenReturn(0);
        when(workoutsRepository.calculateTotalDurationByUserId(userId, dateFrom, dateTo)).thenReturn(0);
        when(workoutsRepository.findWorkoutsByType(userId, dateFrom, dateTo)).thenReturn(Map.of());
        when(workoutsRepository.findMaxWeightByExercise(userId, dateFrom, dateTo)).thenReturn(Map.of());
        when(workoutsRepository.findMaxDistanceByExercise(userId, dateFrom, dateTo)).thenReturn(Map.of());
        when(workoutsRepository.findMaxTimeByExercise(userId, dateFrom, dateTo)).thenReturn(Map.of());
        when(workoutsRepository.calculateMaxCaloriesBurnedInWorkout(userId, dateFrom, dateTo)).thenReturn(0);
        when(workoutsRepository.calculateMaxDurationInWorkout(userId, dateFrom, dateTo)).thenReturn(0);

        AnalyticsResponse response = analyticsService.getAnalytics(request);

        assertNotNull(response);
        assertEquals(0L, response.totalWorkouts());
        assertEquals(0.0, response.totalWeightLifted());
        assertEquals(0, response.totalCaloriesBurned());
        assertEquals(0, response.totalDuration());
        assertEquals(dateFrom, response.periodStart());
        assertEquals(dateTo, response.periodEnd());
        assertEquals(Map.of(), response.workoutsByType());
        assertNotNull(response.maxAchievements());

        verify(workoutsRepository).calculateTotalWorkoutsByUserId(userId, dateFrom, dateTo);
        verify(workoutsRepository).calculateTotalWeightLiftedByUserId(userId, dateFrom, dateTo);
        verify(workoutsRepository).calculateTotalCaloriesBurnedByUserId(userId, dateFrom, dateTo);
        verify(workoutsRepository).calculateTotalDurationByUserId(userId, dateFrom, dateTo);
    }
}

