package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.fitnesstracker.repository.WorkoutsRepository;
import org.example.fitnesstracker.repository.specification.WorkoutSpecifications;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.repository.ExerciseRepository;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.Exercise;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.exception.AccessDeniedException;
import org.example.fitnesstracker.exception.WorkoutNotFoundException;
import org.example.fitnesstracker.exception.UserNotFoundException;
import org.example.fitnesstracker.exception.ExerciseNotFoundException;
import org.example.fitnesstracker.mapper.WorkoutMapper;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutsService {

    private final WorkoutsRepository workoutsRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;


    
    public Page<WorkoutResponse> getAllWorkouts(
        WorkoutType type,
        LocalDate dateFrom, LocalDate dateTo,
        Integer durationFrom, Integer durationTo,
        Integer caloriesFrom, Integer caloriesTo,
        String sortBy, String sortDirection,
        int page, int size
    ) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (sortBy == null) {
            sortBy = "date";
        }
        if (sortDirection == null) {
            sortDirection = "desc";
        }

        size = Math.min(Math.max(size, 1), 100);

        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(Direction.ASC, sortBy) 
            : Sort.by(Direction.DESC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Workout> specification = WorkoutSpecifications.belongsToUser(currentUserId)
            .and(WorkoutSpecifications.hasType(type))
            .and(WorkoutSpecifications.hasDateFrom(dateFrom))
            .and(WorkoutSpecifications.hasDateTo(dateTo))
            .and(WorkoutSpecifications.hasDurationFrom(durationFrom))
            .and(WorkoutSpecifications.hasDurationTo(durationTo))
            .and(WorkoutSpecifications.hasCaloriesFrom(caloriesFrom))
            .and(WorkoutSpecifications.hasCaloriesTo(caloriesTo));

        Page<Workout> workouts = workoutsRepository.findAll(specification, pageable);
        
        return workouts.map(workoutMapper::toResponse);
    }
    

    public WorkoutResponse getWorkoutById(Long id) {
        log.debug("Getting workout by id: {}", id);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Workout workout = workoutsRepository.findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> {
                log.warn("Workout with id {} not found", id);
                return new WorkoutNotFoundException("Workout with id " + id + " not found");
            });
        log.info("Successfully retrieved workout {} for user {}", id, workout.getUser().getId());
        return workoutMapper.toResponse(workout);
    }

    @Transactional
    public WorkoutResponse createWorkout(CreateWorkoutRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Creating workout '{}' for user {}", request.name(), currentUserId);
        
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> {
                log.error("User with id {} not found while creating workout", currentUserId);
                return new UserNotFoundException("User not found with id: " + currentUserId);
            });
        
        Workout workout = workoutMapper.toEntity(request);
        workout.setUser(user);
        
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        if (request.exercises() != null && !request.exercises().isEmpty()) {
            log.debug("Adding {} exercises to workout", request.exercises().size());
            for (CreateWorkoutExerciseRequest exerciseRequest : request.exercises()) {
                Exercise exercise = exerciseRepository.findById(exerciseRequest.exerciseId())
                    .orElseThrow(() -> {
                        log.error("Exercise with id {} not found while creating workout", exerciseRequest.exerciseId());
                        return new ExerciseNotFoundException("Exercise with id " + exerciseRequest.exerciseId() + " not found");
                    });
                
                WorkoutExercise workoutExercise = workoutMapper.toEntity(exerciseRequest);
                workoutExercise.setWorkout(workout);
                workoutExercise.setExercise(exercise);
                workoutExercises.add(workoutExercise);
                log.debug("Added exercise {} (id: {}) to workout", exercise.getName(), exercise.getId());
            }
        }
        workout.setWorkoutExercises(workoutExercises);
        
        Workout savedWorkout = workoutsRepository.save(workout);
        log.info("Successfully created workout {} (id: {}) with {} exercises for user {}", 
            savedWorkout.getName(), savedWorkout.getId(), workoutExercises.size(), currentUserId);
        
        return workoutMapper.toResponse(savedWorkout);
    }

    @Transactional
    public WorkoutResponse updateWorkout(Long id, UpdateWorkoutRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Updating workout {} by user {}", id, currentUserId);
        
        Workout workout = workoutsRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Workout with id {} not found for update by user {}", id, currentUserId);
                return new WorkoutNotFoundException("Workout with id " + id + " not found");
            });
        
        if (!workout.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to update workout {} owned by user {}", 
                currentUserId, id, workout.getUser().getId());
            throw new AccessDeniedException("You can only update your own workouts");
        }
        
        log.debug("Updating workout fields: name={}, type={}, date={}, duration={}, calories={}", 
            request.name(), request.type(), request.date(), request.duration(), request.calories());
        
        workoutMapper.updateEntityFromRequest(request, workout);

        if (request.exercises() != null) {
            updateWorkoutExercises(workout, request.exercises());
        }
        
        Workout updatedWorkout = workoutsRepository.save(workout);
        log.info("Successfully updated workout {} (id: {}) for user {}", 
            updatedWorkout.getName(), updatedWorkout.getId(), currentUserId);
        
        return workoutMapper.toResponse(updatedWorkout);
    }
    
    private void updateWorkoutExercises(Workout workout, List<UpdateWorkoutExerciseRequest> exerciseRequests) {
        List<WorkoutExercise> existingExercises = workout.getWorkoutExercises();
        Map<Long, WorkoutExercise> exerciseMap = existingExercises.stream()
            .collect(Collectors.toMap(WorkoutExercise::getId, ex -> ex));
        
        for (UpdateWorkoutExerciseRequest exerciseRequest : exerciseRequests) {
            WorkoutExercise exercise = exerciseMap.get(exerciseRequest.workoutExerciseId());
            if (exercise != null) {
                workoutMapper.updateExerciseFromRequest(exerciseRequest, exercise);
            }
        }
        
        List<Long> requestedIds = exerciseRequests.stream()
            .map(UpdateWorkoutExerciseRequest::workoutExerciseId)
            .toList();
        
        existingExercises.removeIf(ex -> !requestedIds.contains(ex.getId()));
    }

    @Transactional
    public void deleteWorkout(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Deleting workout {} by user {}", id, currentUserId);
        
        Workout workout = workoutsRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Workout with id {} not found for deletion by user {}", id, currentUserId);
                return new WorkoutNotFoundException("Workout with id " + id + " not found");
            });
        
        if (!workout.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete workout {} owned by user {}", 
                currentUserId, id, workout.getUser().getId());
            throw new AccessDeniedException("You can only delete your own workouts");
        }
        
        log.info("Deleting workout '{}' (id: {}) with {} exercises for user {}", 
            workout.getName(), workout.getId(), workout.getWorkoutExercises().size(), currentUserId);
        workoutsRepository.delete(workout);
        log.info("Successfully deleted workout {} for user {}", id, currentUserId);
    }

}
