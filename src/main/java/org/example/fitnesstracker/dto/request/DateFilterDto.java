package org.example.fitnesstracker.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import org.example.fitnesstracker.validation.ValidDateRange;

import java.time.LocalDate;

@ValidDateRange
public record DateFilterDto(
    @PastOrPresent(message = "Date from must be in the past or present")
    LocalDate dateFrom,
    @PastOrPresent(message = "Date to must be in the past or present")
    LocalDate dateTo
) { }
