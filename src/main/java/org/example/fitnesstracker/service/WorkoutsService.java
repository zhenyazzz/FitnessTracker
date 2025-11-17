package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.fitnesstracker.repository.WorkoutsRepository;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.exception.AccessDeniedException;
import org.example.fitnesstracker.exception.WorkoutNotFoundException;
import org.example.fitnesstracker.mapper.WorkoutMapper;
import org.example.fitnesstracker.security.UserDetailsImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutsService {

    private final WorkoutsRepository workoutsRepository;
    private final WorkoutMapper workoutMapper;

    public WorkoutResponse getWorkoutById(Long id) {
        Workout workout = workoutsRepository.findById(id).orElseThrow(() -> new WorkoutNotFoundException("Workout with id " + id + " not found"));
        return workoutMapper.toResponse(workout);
    }

    @Transactional
    public WorkoutResponse createWorkout(CreateWorkoutRequest request) {
        Workout workout = workoutMapper.toEntity(request);
        workoutsRepository.save(workout);
        return workoutMapper.toResponse(workout);
    }

    @Transactional
    public WorkoutResponse updateWorkout(Long id, UpdateWorkoutRequest request) {
        Workout workout = workoutsRepository.findById(id).orElseThrow(() -> new WorkoutNotFoundException("Workout with id " + id + " not found"));
        Long currentUserId = getCurrentUserId();
        if (!workout.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to update workout {} owned by user {}", 
                currentUserId, id, workout.getUser().getId());
            throw new AccessDeniedException("You can only update your own workouts");
        }
        
        workoutMapper.updateEntityFromRequest(request, workout);
        
        if (request.exercises() != null) {
            updateWorkoutExercises(workout, request.exercises());
        }
        
        workoutsRepository.save(workout);
        return workoutMapper.toResponse(workout);
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
        Workout workout = workoutsRepository.findById(id)
            .orElseThrow(() -> new WorkoutNotFoundException("Workout with id " + id + " not found"));
        
        Long currentUserId = getCurrentUserId();
        
        if (!workout.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to delete workout {} owned by user {}", 
                currentUserId, id, workout.getUser().getId());
            throw new AccessDeniedException("You can only delete your own workouts");
        }
        
        log.info("Deleting workout {} for user {}", id, currentUserId);
        workoutsRepository.delete(workout);
    }

    private Long getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userDetails.getId();
    }

}
