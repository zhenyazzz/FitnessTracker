package org.example.fitnesstracker.dto.request.workouts;

import org.example.fitnesstracker.model.enums.WorkoutType;

import org.example.fitnesstracker.dto.request.DateFilterDto;
import org.example.fitnesstracker.dto.request.DurationFilterDto;
import org.example.fitnesstracker.dto.request.CaloriesFilterDto;
import jakarta.validation.Valid;

public record WorkoutFilterDto(
    WorkoutType type,
    @Valid DateFilterDto dateFilter,   
    @Valid DurationFilterDto durationFilter,
    @Valid CaloriesFilterDto caloriesFilter
 
) {

}
