package org.example.fitnesstracker.dto.request;

import java.time.LocalDate;

public record DateFilterDto(
    LocalDate dateFrom,
    LocalDate dateTo
) { }
