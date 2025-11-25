package org.example.fitnesstracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitnesstracker.controller.docs.WorkoutsControllerApi;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.service.WorkoutsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        @Valid WorkoutFilterDto filter,
        @Valid Pageable pageable
    ) {
        log.debug("Getting workouts with filters: {}", filter);
        Page<WorkoutResponse> result = workoutsService.getAllWorkouts(filter, pageable);
        log.info("Successfully retrieved {} workouts (page {}, total: {})", 
            result.getContent().size(), pageable.getPageNumber(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable Long id) {
        log.debug("Getting workout by id: {}", id);
        WorkoutResponse result = workoutsService.getWorkoutById(id);
        log.info("Successfully retrieved workout with id: {}", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Override
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody CreateWorkoutRequest request) {
        log.debug("Creating workout: name={}, type={}, date={}", request.name(), request.type(), request.date());
        WorkoutResponse result = workoutsService.createWorkout(request);
        log.info("Successfully created workout with id: {}", result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable Long id, @Valid @RequestBody UpdateWorkoutRequest request) {
        log.debug("Updating workout with id: {}", id);
        WorkoutResponse result = workoutsService.updateWorkout(id, request);
        log.info("Successfully updated workout with id: {}", id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        log.debug("Deleting workout with id: {}", id);
        workoutsService.deleteWorkout(id);
        log.info("Successfully deleted workout with id: {}", id);
        return ResponseEntity.noContent().build();
    }

}
