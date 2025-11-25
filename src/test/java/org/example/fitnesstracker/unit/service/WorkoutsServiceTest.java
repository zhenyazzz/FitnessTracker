package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.example.fitnesstracker.dto.request.DateFilterDto;
import org.example.fitnesstracker.dto.request.DurationFilterDto;
import org.example.fitnesstracker.dto.request.CaloriesFilterDto;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.exception.ExerciseNotFoundException;
import org.example.fitnesstracker.exception.UserNotFoundException;
import org.example.fitnesstracker.exception.WorkoutNotFoundException;
import org.example.fitnesstracker.mapper.WorkoutMapper;
import org.example.fitnesstracker.model.Exercise;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.example.fitnesstracker.model.enums.MuscleGroup;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.repository.ExerciseRepository;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.repository.WorkoutsRepository;
import org.example.fitnesstracker.security.SecurityUtils;
import org.example.fitnesstracker.service.WorkoutsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutsService Unit Tests")
class WorkoutsServiceTest {

    @InjectMocks
    private WorkoutsService workoutsService;

    @Mock
    private WorkoutsRepository workoutsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private User testUser;
    private Exercise testExercise;
    private Workout testWorkout;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_WORKOUT_ID = 1L;
    private static final Long TEST_EXERCISE_ID = 1L;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
        
        testUser = User.builder()
            .id(TEST_USER_ID)
            .email("test@example.com")
            .username("testuser")
            .build();

        testExercise = Exercise.builder()
            .id(TEST_EXERCISE_ID)
            .name("Жим лежа")
            .muscleGroup(MuscleGroup.CHEST)
            .description("Базовое упражнение для груди")
            .build();

        testWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name("Утренняя тренировка")
            .type(WorkoutType.STRENGTH)
            .date(LocalDate.now())
            .duration(60)
            .calories(350)
            .user(testUser)
            .workoutExercises(new ArrayList<>())
            .build();
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    @DisplayName("Should get all workouts with pagination")
    void should_GetAllWorkouts_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Workout> workoutPage = new PageImpl<>(List.of(testWorkout), pageable, 1);
        
        WorkoutResponse workoutResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            "Утренняя тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            60,
            350,
            null,
            Collections.emptyList()
        );

        when(workoutsRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(workoutPage);
        when(workoutMapper.toResponse(testWorkout)).thenReturn(workoutResponse);

        // Act
        Page<WorkoutResponse> result = workoutsService.getAllWorkouts(
            new WorkoutFilterDto(null, new DateFilterDto(null, null), new DurationFilterDto(null, null), new CaloriesFilterDto(null, null)), pageable
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get workout by id successfully")
    void should_GetWorkoutById_Successfully() {
        // Arrange
        WorkoutResponse expectedResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            "Утренняя тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            60,
            350,
            null,
            Collections.emptyList()
        );

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        when(workoutMapper.toResponse(testWorkout)).thenReturn(expectedResponse);

        // Act
        WorkoutResponse result = workoutsService.getWorkoutById(TEST_WORKOUT_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_WORKOUT_ID);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutMapper).toResponse(testWorkout);
    }

    @Test
    @DisplayName("Should throw exception when workout not found")
    void should_ThrowException_WhenWorkoutNotFound() {
        // Arrange
        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutsService.getWorkoutById(TEST_WORKOUT_ID))
            .isInstanceOf(WorkoutNotFoundException.class)
            .hasMessageContaining("Workout with id " + TEST_WORKOUT_ID + " not found");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutMapper, never()).toResponse(any());
    }


    @Test
    @DisplayName("Should create a new workout successfully")
    void should_CreateNewWorkout_Successfully() {
        // Arrange
        CreateWorkoutRequest request = new CreateWorkoutRequest(
            "Утренняя тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            60,
            350,
            List.of(
                new CreateWorkoutExerciseRequest(TEST_EXERCISE_ID, 4, 12, 80.0, null, null)
            )
        );

        Workout workoutEntity = Workout.builder()
            .name(request.name())
            .type(request.type())
            .date(request.date())
            .duration(request.duration())
            .calories(request.calories())
            .build();

        WorkoutExercise workoutExerciseEntity = WorkoutExercise.builder()
            .sets(4)
            .reps(12)
            .weight(80.0)
            .build();

        Workout savedWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name(request.name())
            .type(request.type())
            .date(request.date())
            .duration(request.duration())
            .calories(request.calories())
            .user(testUser)
            .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            request.name(),
            request.type(),
            request.date(),
            request.duration(),
            request.calories(),
            null,
            Collections.emptyList()
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(exerciseRepository.findById(TEST_EXERCISE_ID)).thenReturn(Optional.of(testExercise));
        when(workoutMapper.toEntity(request)).thenReturn(workoutEntity);
        when(workoutMapper.toEntity(any(CreateWorkoutExerciseRequest.class))).thenReturn(workoutExerciseEntity);
        when(workoutsRepository.save(any(Workout.class))).thenReturn(savedWorkout);
        when(workoutMapper.toResponse(savedWorkout)).thenReturn(expectedResponse);

        // Act
        WorkoutResponse result = workoutsService.createWorkout(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_WORKOUT_ID);
        assertThat(result.name()).isEqualTo("Утренняя тренировка");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(exerciseRepository).findById(TEST_EXERCISE_ID);
        verify(workoutMapper).toEntity(request);
        verify(workoutsRepository).save(any(Workout.class));
        verify(workoutMapper).toResponse(savedWorkout);
    }

    @Test
    @DisplayName("Should throw exception when user not found during workout creation")
    void should_ThrowException_WhenUserNotFound() {
        // Arrange
        CreateWorkoutRequest request = new CreateWorkoutRequest(
            "Утренняя тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            60,
            350,
            null
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutsService.createWorkout(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: " + TEST_USER_ID);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(workoutsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when exercise not found during workout creation")
    void should_ThrowException_WhenExerciseNotFound() {
        // Arrange
        CreateWorkoutRequest request = new CreateWorkoutRequest(
            "Утренняя тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            60,
            350,
            List.of(new CreateWorkoutExerciseRequest(TEST_EXERCISE_ID, 4, 12, 80.0, null, null))
        );

        Workout workoutEntity = Workout.builder()
            .name(request.name())
            .type(request.type())
            .date(request.date())
            .duration(request.duration())
            .calories(request.calories())
            .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(workoutMapper.toEntity(request)).thenReturn(workoutEntity);
        when(exerciseRepository.findById(TEST_EXERCISE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutsService.createWorkout(request))
            .isInstanceOf(ExerciseNotFoundException.class)
            .hasMessageContaining("Exercise with id " + TEST_EXERCISE_ID + " not found");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(exerciseRepository).findById(TEST_EXERCISE_ID);
        verify(workoutsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update workout successfully without exercises")
    void should_UpdateWorkout_Successfully_WithoutExercises() {
        // Arrange
        UpdateWorkoutRequest request = new UpdateWorkoutRequest(
            "Обновленная тренировка",
            WorkoutType.CARDIO,
            LocalDate.now().minusDays(1),
            75,
            450
        );

        Workout updatedWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name(request.name())
            .type(request.type())
            .date(request.date())
            .duration(request.duration())
            .calories(request.calories())
            .user(testUser)
            .workoutExercises(new ArrayList<>())
            .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            request.name(),
            request.type(),
            request.date(),
            request.duration(),
            request.calories(),
            null,
            Collections.emptyList()
        );

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        doNothing().when(workoutMapper).updateEntityFromRequest(request, testWorkout);
        when(workoutsRepository.save(testWorkout)).thenReturn(updatedWorkout);
        when(workoutMapper.toResponse(updatedWorkout)).thenReturn(expectedResponse);

        // Act
        WorkoutResponse result = workoutsService.updateWorkout(TEST_WORKOUT_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_WORKOUT_ID);
        assertThat(result.name()).isEqualTo("Обновленная тренировка");
        assertThat(result.type()).isEqualTo(WorkoutType.CARDIO);
        assertThat(result.duration()).isEqualTo(75);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutMapper).updateEntityFromRequest(request, testWorkout);
        verify(workoutsRepository).save(testWorkout);
        verify(workoutMapper).toResponse(updatedWorkout);
    }

    @Test
    @DisplayName("Should update workout successfully with exercises")
    void should_UpdateWorkout_Successfully_WithExercises() {
        // Arrange
        Long workoutExerciseId = 1L;
        WorkoutExercise existingExercise = WorkoutExercise.builder()
            .id(workoutExerciseId)
            .workout(testWorkout)
            .exercise(testExercise)
            .sets(4)
            .reps(12)
            .weight(80.0)
            .build();
        
        testWorkout.setWorkoutExercises(new ArrayList<>(List.of(existingExercise)));

        UpdateWorkoutRequest request = new UpdateWorkoutRequest(
            "Обновленная тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            90,
            500
        );

        Workout updatedWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name(request.name())
            .type(request.type())
            .date(request.date())
            .duration(request.duration())
            .calories(request.calories())
            .user(testUser)
            .workoutExercises(testWorkout.getWorkoutExercises())
            .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            request.name(),
            request.type(),
            request.date(),
            request.duration(),
            request.calories(),
            null,
            Collections.emptyList()
        );

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        doNothing().when(workoutMapper).updateEntityFromRequest(request, testWorkout);
        when(workoutsRepository.save(testWorkout)).thenReturn(updatedWorkout);
        when(workoutMapper.toResponse(updatedWorkout)).thenReturn(expectedResponse);

        // Act
        WorkoutResponse result = workoutsService.updateWorkout(TEST_WORKOUT_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_WORKOUT_ID);
        assertThat(result.name()).isEqualTo("Обновленная тренировка");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutMapper).updateEntityFromRequest(request, testWorkout);
        verify(workoutsRepository).save(testWorkout);
        verify(workoutMapper).toResponse(updatedWorkout);
    }

    @Test
    @DisplayName("Should throw exception when workout not found during update")
    void should_ThrowException_WhenWorkoutNotFound_DuringUpdate() {
        // Arrange
        UpdateWorkoutRequest request = new UpdateWorkoutRequest(
            "Обновленная тренировка",
            WorkoutType.STRENGTH,
            LocalDate.now(),
            75,
            450
        );

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutsService.updateWorkout(TEST_WORKOUT_ID, request))
            .isInstanceOf(WorkoutNotFoundException.class)
            .hasMessageContaining("Workout with id " + TEST_WORKOUT_ID + " not found");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutMapper, never()).updateEntityFromRequest(any(), any());
        verify(workoutsRepository, never()).save(any());
    }
        
    @Test
    @DisplayName("Should delete workout successfully")
    void should_DeleteWorkout_Successfully() {
        // Arrange
        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        doNothing().when(workoutsRepository).delete(testWorkout);

        // Act
        workoutsService.deleteWorkout(TEST_WORKOUT_ID);

        // Assert
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutsRepository).delete(testWorkout);
    }

    @Test
    @DisplayName("Should throw exception when workout not found during deletion")
    void should_ThrowException_WhenWorkoutNotFound_DuringDeletion() {
        // Arrange
        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> workoutsService.deleteWorkout(TEST_WORKOUT_ID))
            .isInstanceOf(WorkoutNotFoundException.class)
            .hasMessageContaining("Workout with id " + TEST_WORKOUT_ID + " not found");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutsRepository, never()).delete(any(Workout.class));
    }

    @Test
    @DisplayName("Should add exercise to workout successfully")
    void should_AddExerciseToWorkout_Successfully() {
        // Arrange
        CreateWorkoutExerciseRequest request = new CreateWorkoutExerciseRequest(
            TEST_EXERCISE_ID, 4, 12, 80.0, null, null
        );

        WorkoutExercise workoutExercise = WorkoutExercise.builder()
            .id(1L)
            .workout(testWorkout)
            .exercise(testExercise)
            .sets(4)
            .reps(12)
            .weight(80.0)
            .build();

        Workout savedWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name(testWorkout.getName())
            .type(testWorkout.getType())
            .date(testWorkout.getDate())
            .duration(testWorkout.getDuration())
            .calories(testWorkout.getCalories())
            .user(testUser)
            .workoutExercises(new ArrayList<>(List.of(workoutExercise)))
            .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
            TEST_WORKOUT_ID,
            testWorkout.getName(),
            testWorkout.getType(),
            testWorkout.getDate(),
            testWorkout.getDuration(),
            testWorkout.getCalories(),
            null,
            Collections.emptyList()
        );

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        when(exerciseRepository.findById(TEST_EXERCISE_ID)).thenReturn(Optional.of(testExercise));
        when(workoutMapper.toEntity(request)).thenReturn(workoutExercise);
        when(workoutsRepository.save(testWorkout)).thenReturn(savedWorkout);
        when(workoutMapper.toResponse(savedWorkout)).thenReturn(expectedResponse);

        // Act
        WorkoutResponse result = workoutsService.addExerciseToWorkout(TEST_WORKOUT_ID, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_WORKOUT_ID);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(exerciseRepository).findById(TEST_EXERCISE_ID);
        verify(workoutMapper).toEntity(request);
        verify(workoutsRepository).save(testWorkout);
        verify(workoutMapper).toResponse(savedWorkout);
    }

    @Test
    @DisplayName("Should remove exercise from workout successfully")
    void should_RemoveExerciseFromWorkout_Successfully() {
        // Arrange
        Long workoutExerciseId = 1L;
        WorkoutExercise workoutExercise = WorkoutExercise.builder()
            .id(workoutExerciseId)
            .workout(testWorkout)
            .exercise(testExercise)
            .sets(4)
            .reps(12)
            .weight(80.0)
            .build();

        testWorkout.setWorkoutExercises(new ArrayList<>(List.of(workoutExercise)));

        Workout savedWorkout = Workout.builder()
            .id(TEST_WORKOUT_ID)
            .name(testWorkout.getName())
            .type(testWorkout.getType())
            .date(testWorkout.getDate())
            .duration(testWorkout.getDuration())
            .calories(testWorkout.getCalories())
            .user(testUser)
            .workoutExercises(new ArrayList<>())
            .build();

        when(workoutsRepository.findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID)).thenReturn(Optional.of(testWorkout));
        when(workoutsRepository.save(testWorkout)).thenReturn(savedWorkout);

        // Act
        workoutsService.removeExerciseFromWorkout(TEST_WORKOUT_ID, workoutExerciseId);

        // Assert
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(workoutsRepository).findByIdAndUserId(TEST_WORKOUT_ID, TEST_USER_ID);
        verify(workoutsRepository).save(testWorkout);
    }

}
