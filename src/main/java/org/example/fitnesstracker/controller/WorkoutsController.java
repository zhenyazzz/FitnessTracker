package org.example.fitnesstracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.example.fitnesstracker.service.WorkoutsService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.model.enums.WorkoutType;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "API для работы с тренировками")
public class WorkoutsController {

    private final WorkoutsService workoutsService;

    @GetMapping
    public ResponseEntity<Page<WorkoutResponse>> getAllWorkouts(
        @RequestParam(required = false) WorkoutType type,
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo,
        @RequestParam(required = false) Integer durationFrom,
        @RequestParam(required = false) Integer durationTo,
        @RequestParam(required = false) Integer caloriesFrom,
        @RequestParam(required = false) Integer caloriesTo,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(workoutsService.getAllWorkouts(type, dateFrom, dateTo, durationFrom, durationTo, caloriesFrom, caloriesTo, sortBy, sortDirection, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutsService.getWorkoutById(id));
    }

    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody CreateWorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutsService.createWorkout(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable Long id, @Valid @RequestBody UpdateWorkoutRequest request) {
        return ResponseEntity.ok(workoutsService.updateWorkout(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutsService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

}
