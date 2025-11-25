package org.example.fitnesstracker.dto.request;

import jakarta.validation.constraints.Min;
import org.example.fitnesstracker.validation.ValidRange;

@ValidRange(fromField = "durationFrom", toField = "durationTo", message = "DurationFrom must be less than or equal to DurationTo")
public record DurationFilterDto(
    @Min(value = 0, message = "Duration from must be greater than 0")
    Integer durationFrom,
    @Min(value = 0, message = "Duration to must be greater than 0")
    Integer durationTo
) {

}
