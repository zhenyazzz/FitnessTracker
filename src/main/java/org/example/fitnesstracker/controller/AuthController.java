package org.example.fitnesstracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitnesstracker.controller.docs.AuthControllerApi;
import org.example.fitnesstracker.dto.request.auth.LoginRequest;
import org.example.fitnesstracker.dto.request.auth.LogoutRequest;
import org.example.fitnesstracker.dto.request.auth.RefreshTokenRequest;
import org.example.fitnesstracker.dto.request.auth.RegisterRequest;
import org.example.fitnesstracker.dto.response.AuthResponse;
import org.example.fitnesstracker.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @PostMapping("/register")
    @Override
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Registering user with email: {}", request.email());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Logging in user with email: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refreshing token for user with email: {}", request.refreshToken());
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        log.debug("Logging out user with refresh token: {}", request.refreshToken());
        authService.logout(request);
        
        log.debug("User logged out successfully with refresh token: {}", request.refreshToken());
        return ResponseEntity.noContent().build();
    }

}
