package org.example.fitnesstracker.dto.request.media;

import jakarta.validation.constraints.Size;

public record MediaRequest(
    @Size(max = 500, message = "Note cannot exceed 500 characters")
    String note
) {

}
