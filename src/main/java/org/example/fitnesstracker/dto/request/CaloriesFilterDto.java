package org.example.fitnesstracker.dto.request;

import jakarta.validation.constraints.Min;
import org.example.fitnesstracker.validation.ValidRange;

@ValidRange(fromField = "caloriesFrom", toField = "caloriesTo", message = "CaloriesFrom must be less than or equal to CaloriesTo")
public record CaloriesFilterDto(
    @Min(value = 0, message = "Calories from must be greater than 0")
    Integer caloriesFrom,
    @Min(value = 0, message = "Calories to must be greater than 0")
    Integer caloriesTo
) { }
