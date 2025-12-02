package org.example.fitnesstracker.service;

import org.example.fitnesstracker.dto.request.auth.LoginRequest;
import org.example.fitnesstracker.dto.request.auth.LogoutRequest;
import org.example.fitnesstracker.dto.request.auth.RefreshTokenRequest;
import org.example.fitnesstracker.dto.request.auth.RegisterRequest;
import org.example.fitnesstracker.dto.response.AuthResponse;
import org.example.fitnesstracker.exception.EmailAlreadyExistsException;
import org.example.fitnesstracker.exception.RefreshTokenExpiredException;
import org.example.fitnesstracker.exception.RefreshTokenNotFoundException;
import org.example.fitnesstracker.exception.RoleNotFoundException;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.enums.RoleName;
import org.example.fitnesstracker.model.Role;
import org.example.fitnesstracker.model.RefreshToken;
import org.example.fitnesstracker.repository.RefreshTokenRepository;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.repository.RoleRepository;
import org.example.fitnesstracker.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("User with email " + request.email() + " already exists");
        }
        
        User user = createAndSaveUser(request);
        AuthResponse tokens = generateAndSaveTokens(user);
    
        return tokens;
    }

    private User createAndSaveUser(RegisterRequest request) {
        Role userRole = roleRepository.findByName(RoleName.USER)
            .orElseThrow(() -> new RoleNotFoundException("Role USER not found in database. Please run database migrations."));

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .username(request.username())
            .build();
        
        user.getRoles().add(userRole);
        
        userRepository.save(user);
        return user;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException("User with email " + request.email() + " not found"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        
        refreshTokenRepository.deleteByUser(user);
        AuthResponse tokens = generateAndSaveTokens(user);
    
        return tokens;        
    }

    private AuthResponse generateAndSaveTokens(User user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken  = jwtService.generateAccessToken(user);
    
        RefreshToken refreshed = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(jwtService.extractExpirationLocal(refreshToken))
                .build();
        refreshTokenRepository.save(refreshed);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found. Please login again."));

        if (jwtService.isTokenExpired(request.refreshToken())) {
            refreshTokenRepository.delete(stored);
            throw new RefreshTokenExpiredException("Refresh token expired. Please login again.");
        }

        User user = stored.getUser();

        String newRefresh = jwtService.generateRefreshToken(user);
        String newAccess  = jwtService.generateAccessToken(user);

        stored.setToken(newRefresh);
        stored.setExpiryDate(jwtService.extractExpirationLocal(newRefresh));

        refreshTokenRepository.save(stored);

        return new AuthResponse(newAccess, newRefresh);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token " + request.refreshToken() + " not found"));
        
        refreshTokenRepository.delete(refreshToken);
    }

}
