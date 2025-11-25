package org.example.fitnesstracker.dto.request.workouts;

import org.example.fitnesstracker.model.enums.WorkoutType;

import org.example.fitnesstracker.dto.request.DateFilterDto;
import org.example.fitnesstracker.dto.request.DurationFilterDto;
import org.example.fitnesstracker.dto.request.CaloriesFilterDto;

public record WorkoutFilterDto(
    WorkoutType type,
    DateFilterDto dateFilter,   
    DurationFilterDto durationFilter,
    CaloriesFilterDto caloriesFilter
 
) {

}
