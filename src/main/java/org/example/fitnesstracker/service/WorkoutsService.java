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
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.Exercise;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.exception.WorkoutNotFoundException;
import org.example.fitnesstracker.exception.UserNotFoundException;
import org.example.fitnesstracker.exception.ExerciseNotFoundException;
import org.example.fitnesstracker.mapper.WorkoutMapper;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutsService {

    private final WorkoutsRepository workoutsRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;

    
    @Transactional(readOnly = true)
    public Page<WorkoutResponse> getAllWorkouts(
        WorkoutFilterDto filter,
        Pageable pageable
    ) {
        Specification<Workout> specification = buildSpecification(filter);
        
        Page<Workout> workouts = workoutsRepository.findAll(specification, pageable);
        
        return workouts.map(workoutMapper::toResponse);
    }

    private Specification<Workout> buildSpecification(WorkoutFilterDto filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Specification<Workout> specification = WorkoutSpecifications.belongsToUser(currentUserId);
        
        if (filter != null) {
            if (filter.type() != null) {
                specification = specification.and(WorkoutSpecifications.hasType(filter.type()));
            }
            
            if (filter.dateFilter() != null) {
                specification = specification
                    .and(WorkoutSpecifications.hasDateFrom(filter.dateFilter().dateFrom()))
                    .and(WorkoutSpecifications.hasDateTo(filter.dateFilter().dateTo()));
            }
            
            if (filter.durationFilter() != null) {
                specification = specification
                    .and(WorkoutSpecifications.hasDurationFrom(filter.durationFilter().durationFrom()))
                    .and(WorkoutSpecifications.hasDurationTo(filter.durationFilter().durationTo()));
            }
            
            if (filter.caloriesFilter() != null) {
                specification = specification
                    .and(WorkoutSpecifications.hasCaloriesFrom(filter.caloriesFilter().caloriesFrom()))
                    .and(WorkoutSpecifications.hasCaloriesTo(filter.caloriesFilter().caloriesTo()));
            }
        }
        
        return specification;
    }

    @Transactional(readOnly = true)
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
        
        List<WorkoutExercise> workoutExercises = createWorkoutExercises(workout, request.exercises());

        workout.setWorkoutExercises(workoutExercises);
        
        Workout savedWorkout = workoutsRepository.save(workout);
        log.info("Successfully created workout {} (id: {}) with {} exercises for user {}", 
            savedWorkout.getName(), savedWorkout.getId(), workoutExercises.size(), currentUserId);
        
        return workoutMapper.toResponse(savedWorkout);
    }

    private List<WorkoutExercise> createWorkoutExercises(Workout workout, List<CreateWorkoutExerciseRequest> exerciseRequests) {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        if (exerciseRequests != null && !exerciseRequests.isEmpty()) {
            log.debug("Adding {} exercises to workout", exerciseRequests.size());
            for (CreateWorkoutExerciseRequest exerciseRequest : exerciseRequests) {
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
        return workoutExercises;
    }

    @Transactional
    public WorkoutResponse updateWorkout(Long id, UpdateWorkoutRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Updating workout {} by user {}", id, currentUserId);
        
        Workout workout = workoutsRepository.findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> {
                log.warn("Workout with id {} not found for user {}", id, currentUserId);
                return new WorkoutNotFoundException("Workout with id " + id + " not found");
            });
        
        log.debug("Updating workout fields: name={}, type={}, date={}, duration={}, calories={}", 
            request.name(), request.type(), request.date(), request.duration(), request.calories());
        
        workoutMapper.updateEntityFromRequest(request, workout);
        
        Workout updatedWorkout = workoutsRepository.save(workout);
        log.info("Successfully updated workout {} (id: {}) for user {}", 
            updatedWorkout.getName(), updatedWorkout.getId(), currentUserId);
        
        return workoutMapper.toResponse(updatedWorkout);
    }

    @Transactional
    public void deleteWorkout(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Deleting workout {} by user {}", id, currentUserId);
        
        Workout workout = workoutsRepository.findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> {
                log.warn("Workout with id {} not found for user {}", id, currentUserId);
                return new WorkoutNotFoundException("Workout with id " + id + " not found");
            });
        
        workoutsRepository.delete(workout);
        log.info("Successfully deleted workout {} for user {}", id, currentUserId);
    }

}
