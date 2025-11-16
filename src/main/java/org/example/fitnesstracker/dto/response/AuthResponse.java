package org.example.fitnesstracker.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {

}
