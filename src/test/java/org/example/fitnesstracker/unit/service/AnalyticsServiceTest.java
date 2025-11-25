package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.dto.response.analytics.MaxAchievements;
import org.example.fitnesstracker.model.Exercise;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.example.fitnesstracker.model.enums.MuscleGroup;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Test")
public class AnalyticsServiceTest {

    @Mock
    private WorkoutsRepository workoutsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Long userId;
    private User testUser;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        userId = 1L;
        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("testuser")
                .build();
        
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
    @DisplayName("Should return empty analytics when no workouts")
    void shouldReturnEmptyAnalyticsWhenNoWorkouts() {
        // Arrange
        AnalyticsRequest request = new AnalyticsRequest(null);
        @SuppressWarnings("unchecked")
        Specification<Workout> spec = any(Specification.class);
        when(workoutsRepository.findAll(spec)).thenReturn(Collections.emptyList());

        // Act
        AnalyticsResponse response = analyticsService.getAnalytics(request);

        // Assert
        assertNotNull(response);
        assertEquals(0L, response.totalWorkouts());
        assertEquals(0.0, response.totalWeightLifted());
        assertEquals(0, response.totalCaloriesBurned());
        assertEquals(0, response.totalDuration());
        assertNull(response.periodStart());
        assertNull(response.periodEnd());
        assertTrue(response.workoutsByType().isEmpty());
    }

    @Test
    @DisplayName("Should correctly calculate analytics for a single workout")
    void shouldCalculateAnalyticsForSingleWorkout() {
        // Arrange
        AnalyticsRequest request = new AnalyticsRequest(null);
        Workout workout = createWorkout(1L, "Утренняя пробежка", WorkoutType.RUNNING, 
                LocalDate.now(), 30, 200);
        
        @SuppressWarnings("unchecked")
        Specification<Workout> spec = any(Specification.class);
        when(workoutsRepository.findAll(spec)).thenReturn(List.of(workout));

        // Act
        AnalyticsResponse response = analyticsService.getAnalytics(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.totalWorkouts());
        assertEquals(0.0, response.totalWeightLifted()); 
        assertEquals(200, response.totalCaloriesBurned());
        assertEquals(30, response.totalDuration());
        assertNull(response.periodStart());
        assertNull(response.periodEnd());
        assertEquals(1, response.workoutsByType().get(WorkoutType.RUNNING));
    }

    @Test
    @DisplayName("Should correctly calculate total weight lifted")
    void shouldCalculateTotalWeightLifted() {
        // Arrange
        AnalyticsRequest request = new AnalyticsRequest(null);
        Exercise benchPress = createExercise(1L, "Жим лежа", MuscleGroup.CHEST);
        Exercise squat = createExercise(2L, "Приседания", MuscleGroup.LEGS);

        WorkoutExercise exercise1 = createWorkoutExercise(1L, benchPress, 3, 10, 80.0, null, null);
        WorkoutExercise exercise2 = createWorkoutExercise(2L, squat, 4, 12, 100.0, null, null);

        Workout workout = createWorkoutWithExercises(1L, "Силовая тренировка", 
                WorkoutType.STRENGTH, LocalDate.now(), 60, 400, 
                List.of(exercise1, exercise2));

        @SuppressWarnings("unchecked")
        Specification<Workout> spec = any(Specification.class);
        when(workoutsRepository.findAll(spec)).thenReturn(List.of(workout));

        // Act
        AnalyticsResponse response = analyticsService.getAnalytics(request);

        // Assert
        assertEquals(7200.0, response.totalWeightLifted(), 0.01);
    }

    @Test
    @DisplayName("Should correctly calculate max achievements")
    void shouldCalculateMaxAchievements() {
        // Arrange
        AnalyticsRequest request = new AnalyticsRequest(null);
        Exercise benchPress = createExercise(1L, "Жим лежа", MuscleGroup.CHEST);
        Exercise deadlift = createExercise(2L, "Становая тяга", MuscleGroup.BACK);
        Exercise running = createExercise(3L, "Бег", MuscleGroup.FULL_BODY);

        WorkoutExercise exercise1 = createWorkoutExercise(1L, benchPress, 3, 10, 80.0, null, null);
        WorkoutExercise exercise2 = createWorkoutExercise(2L, deadlift, 1, 5, 150.0, null, null);
        WorkoutExercise exercise3 = createWorkoutExercise(3L, running, null, null, null, 5000.0, 1800);

        Workout workout1 = createWorkoutWithExercises(1L, "Силовая 1", WorkoutType.STRENGTH, 
                LocalDate.now().minusDays(2), 60, 400, List.of(exercise1));
        
        Workout workout2 = createWorkoutWithExercises(2L, "Силовая 2", WorkoutType.STRENGTH, 
                LocalDate.now().minusDays(1), 70, 500, List.of(exercise2));
        
        Workout workout3 = createWorkoutWithExercises(3L, "Кардио", WorkoutType.CARDIO, 
                LocalDate.now(), 45, 600, List.of(exercise3));

        @SuppressWarnings("unchecked")
        Specification<Workout> spec = any(Specification.class);
        when(workoutsRepository.findAll(spec))
                .thenReturn(List.of(workout1, workout2, workout3));

        // Act
        AnalyticsResponse response = analyticsService.getAnalytics(request);

        // Assert
        MaxAchievements maxAchievements = response.maxAchievements();
        assertNotNull(maxAchievements);
        
        assertEquals(80, maxAchievements.maxWeightByExercise().get("Жим лежа"));
        assertEquals(150, maxAchievements.maxWeightByExercise().get("Становая тяга"));
        assertEquals(5000, maxAchievements.maxDistanceByExercise().get("Бег"));
        assertEquals(1800, maxAchievements.maxTimeByExercise().get("Бег"));
        assertEquals(600, maxAchievements.maxCaloriesBurnedInWorkout());
        assertEquals(70, maxAchievements.maxDurationInWorkout());
    }

    @Test
    @DisplayName("Should correctly group workouts by type")
    void shouldGroupWorkoutsByType() {
        // Arrange
        AnalyticsRequest request = new AnalyticsRequest(null);
        Workout workout1 = createWorkout(1L, "Силовая 1", WorkoutType.STRENGTH, 
                LocalDate.now(), 60, 400);
        Workout workout2 = createWorkout(2L, "Силовая 2", WorkoutType.STRENGTH, 
                LocalDate.now(), 50, 350);
        Workout workout3 = createWorkout(3L, "Кардио", WorkoutType.CARDIO, 
                LocalDate.now(), 30, 200);
        Workout workout4 = createWorkout(4L, "Йога", WorkoutType.YOGA, 
                LocalDate.now(), 45, 100);

        @SuppressWarnings("unchecked")
        Specification<Workout> spec = any(Specification.class);
        when(workoutsRepository.findAll(spec))
                .thenReturn(List.of(workout1, workout2, workout3, workout4));

        // Act
        AnalyticsResponse response = analyticsService.getAnalytics(request);

        // Assert
        Map<WorkoutType, Integer> workoutsByType = response.workoutsByType();
        assertEquals(2, workoutsByType.get(WorkoutType.STRENGTH));
        assertEquals(1, workoutsByType.get(WorkoutType.CARDIO));
        assertEquals(1, workoutsByType.get(WorkoutType.YOGA));
    }


    private Workout createWorkout(Long id, String name, WorkoutType type, 
                                   LocalDate date, Integer duration, Integer calories) {
        Workout workout = Workout.builder()
                .id(id)
                .user(testUser)
                .name(name)
                .type(type)
                .date(date)
                .duration(duration)
                .calories(calories)
                .workoutExercises(new ArrayList<>())
                .build();
        return workout;
    }

    private Workout createWorkoutWithExercises(Long id, String name, WorkoutType type,
                                                LocalDate date, Integer duration, Integer calories,
                                                List<WorkoutExercise> exercises) {
        Workout workout = createWorkout(id, name, type, date, duration, calories);
        workout.setWorkoutExercises(exercises);
        exercises.forEach(ex -> ex.setWorkout(workout));
        return workout;
    }

    private Exercise createExercise(Long id, String name, MuscleGroup muscleGroup) {
        return Exercise.builder()
                .id(id)
                .name(name)
                .muscleGroup(muscleGroup)
                .build();
    }

    private WorkoutExercise createWorkoutExercise(Long id, Exercise exercise, 
                                                   Integer sets, Integer reps, Double weight,
                                                   Double distance, Integer time) {
        return WorkoutExercise.builder()
                .id(id)
                .exercise(exercise)
                .sets(sets)
                .reps(reps)
                .weight(weight)
                .distance(distance)
                .time(time)
                .build();
    }
}
