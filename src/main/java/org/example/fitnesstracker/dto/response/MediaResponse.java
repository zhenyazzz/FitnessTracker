package org.example.fitnesstracker.dto.response;

import java.time.LocalDateTime;

public record MediaResponse(
    Long id,
    String presignedUrl,  
    String note,
    Long fileSize,
    String mimeType,
    LocalDateTime createdAt
) {

}
