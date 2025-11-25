package org.example.fitnesstracker.dto.request.media;

import org.example.fitnesstracker.dto.request.DateFilterDto;
import jakarta.validation.Valid;

public record MediaFilterDto(
    @Valid DateFilterDto dateFilter
) {

}
