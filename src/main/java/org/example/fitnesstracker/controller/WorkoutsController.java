package org.example.fitnesstracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitnesstracker.controller.docs.WorkoutsControllerApi;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.service.WorkoutsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@Slf4j
public class WorkoutsController implements WorkoutsControllerApi {

    private final WorkoutsService workoutsService;

    @GetMapping
    @Override
    public ResponseEntity<Page<WorkoutResponse>> getAllWorkouts(
        @Valid @RequestBody WorkoutFilterDto filter,
        @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<WorkoutResponse> result = workoutsService.getAllWorkouts(filter, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable Long id) {
        WorkoutResponse result = workoutsService.getWorkoutById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Override
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody CreateWorkoutRequest request) {
        WorkoutResponse result = workoutsService.createWorkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable Long id, @Valid @RequestBody UpdateWorkoutRequest request) {
        WorkoutResponse result = workoutsService.updateWorkout(id, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutsService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/exercises")
    @Override
    public ResponseEntity<WorkoutResponse> addExerciseToWorkout(@PathVariable Long id, @Valid @RequestBody CreateWorkoutExerciseRequest request) {
        WorkoutResponse result = workoutsService.addExerciseToWorkout(id, request);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}/exercises/{exerciseId}")
    @Override
    public ResponseEntity<Void> removeExerciseFromWorkout(@PathVariable Long id, @PathVariable Long exerciseId) {
        workoutsService.removeExerciseFromWorkout(id, exerciseId);
        return ResponseEntity.noContent().build();
    }
}
