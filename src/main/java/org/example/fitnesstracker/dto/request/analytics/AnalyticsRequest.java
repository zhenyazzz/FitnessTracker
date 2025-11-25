package org.example.fitnesstracker.dto.request.analytics;

import jakarta.validation.Valid;
import org.example.fitnesstracker.dto.request.DateFilterDto;

public record AnalyticsRequest(
    @Valid DateFilterDto dateFilter
) {

}
