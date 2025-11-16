package org.example.fitnesstracker.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    String password,
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    String username
) {

}
