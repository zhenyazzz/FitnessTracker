package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;

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
import org.example.fitnesstracker.exception.WorkoutExerciseNotFoundException;
import org.example.fitnesstracker.exception.ExerciseNotFoundException;
import org.example.fitnesstracker.mapper.WorkoutMapper;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
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
        Specification<Workout> specification = WorkoutSpecifications.buildSpecification(filter);
        
        Page<Workout> workouts = workoutsRepository.findAll(specification, pageable);
        
        return workouts.map(workoutMapper::toResponse);
    }


    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Workout workout = findWorkoutByIdAndUserId(id, currentUserId);
        return workoutMapper.toResponse(workout);
    }

    @Transactional
    public WorkoutResponse createWorkout(CreateWorkoutRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + currentUserId));
        
        Workout workout = workoutMapper.toEntity(request);
        workout.setUser(user);
        
        List<WorkoutExercise> workoutExercises = createWorkoutExercises(workout, request.exercises());

        workout.setWorkoutExercises(workoutExercises);
        
        Workout savedWorkout = workoutsRepository.save(workout);
        
        return workoutMapper.toResponse(savedWorkout);
    }

    private List<WorkoutExercise> createWorkoutExercises(Workout workout, List<CreateWorkoutExerciseRequest> exerciseRequests) {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        if (exerciseRequests != null && !exerciseRequests.isEmpty()) {
            for (CreateWorkoutExerciseRequest exerciseRequest : exerciseRequests) {
                Exercise exercise = findExerciseById(exerciseRequest.exerciseId());
                
                WorkoutExercise workoutExercise = workoutMapper.toEntity(exerciseRequest);
                workoutExercise.setWorkout(workout);
                workoutExercise.setExercise(exercise);
                workoutExercises.add(workoutExercise);
            }
        }
        return workoutExercises;
    }

    @Transactional
    public WorkoutResponse updateWorkout(Long id, UpdateWorkoutRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Workout workout = findWorkoutByIdAndUserId(id, currentUserId);
        
        workoutMapper.updateEntityFromRequest(request, workout);
        
        Workout updatedWorkout = workoutsRepository.save(workout);
        
        return workoutMapper.toResponse(updatedWorkout);
    }

    @Transactional
    public void deleteWorkout(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Workout workout = findWorkoutByIdAndUserId(id, currentUserId);
        
        workoutsRepository.delete(workout);
    }

    @Transactional
    public WorkoutResponse addExerciseToWorkout(Long workoutId, CreateWorkoutExerciseRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Workout workout = findWorkoutByIdAndUserId(workoutId, currentUserId);
        Exercise exercise = findExerciseById(request.exerciseId());

        WorkoutExercise workoutExercise = workoutMapper.toEntity(request);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        
        workout.getWorkoutExercises().add(workoutExercise);
        Workout savedWorkout = workoutsRepository.save(workout);
        
        return workoutMapper.toResponse(savedWorkout);
    }

    @Transactional
    public void removeExerciseFromWorkout(Long workoutId, Long workoutExerciseId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        Workout workout = findWorkoutByIdAndUserId(workoutId, currentUserId);
        
        boolean removed = workout.getWorkoutExercises().removeIf(we -> we.getId().equals(workoutExerciseId));
        if (!removed) {
            throw new WorkoutExerciseNotFoundException("Workout exercise with id " + workoutExerciseId + " not found in workout " + workoutId);
        }

        workoutsRepository.save(workout);
    }

    private Workout findWorkoutByIdAndUserId(Long workoutId, Long userId) {
        return workoutsRepository.findByIdAndUserId(workoutId, userId)
            .orElseThrow(() -> new WorkoutNotFoundException("Workout with id " + workoutId + " not found"));
    }

    private Exercise findExerciseById(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ExerciseNotFoundException("Exercise with id " + exerciseId + " not found"));
    }
}
