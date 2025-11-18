package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.model.Role;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.enums.RoleName;
import org.example.fitnesstracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    
    
    private static final String SECRET_KEY = "Zml0bmVzc1RyYWNrZXJTZWN1cml0eUtleTIwMjUwMDEwMTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo=";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 час 
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 дней 
    
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY);
        
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
        
        Role userRole = Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .build();
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .username("testuser")
                .roles(roles)
                .build();
        
    }

    
    @Test
    @DisplayName("Should generate access token from User")
    void should_GenerateAccessToken_FromUser() {
        // Act
        String token = jwtService.generateAccessToken(testUser);
        
        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@example.com");
        
        List<String> roles = jwtService.extractRoles(token);
        assertThat(roles).contains("USER");
    }

    @Test
    @DisplayName("Should generate refresh token from User")
    void should_GenerateRefreshToken_FromUser() {
        // Act
        String token = jwtService.generateRefreshToken(testUser);
        
        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        
        
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@example.com");
        
        List<String> roles = jwtService.extractRoles(token);
        assertThat(roles).isEmpty();
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void should_ExtractUsername_FromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        
        // Act
        String username = jwtService.extractUsername(token);
        
        // Assert
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void should_ExtractExpiration_FromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        
        // Act
        Date expiration = jwtService.extractExpiration(token);
        
        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract expiration as LocalDateTime from token")
    void should_ExtractExpirationLocal_FromToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        
        // Act
        LocalDateTime expiration = jwtService.extractExpirationLocal(token);
        
        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should extract roles from access token")
    void should_ExtractRoles_FromAccessToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        
        // Act
        List<String> roles = jwtService.extractRoles(token);
        
        // Assert
        assertThat(roles).isNotNull();
        assertThat(roles).contains("USER");
    }

    @Test
    @DisplayName("Should return empty list when extracting roles from refresh token")
    void should_ReturnEmptyList_WhenExtractingRolesFromRefreshToken() {
        // Arrange
        String token = jwtService.generateRefreshToken(testUser);
        
        // Act
        List<String> roles = jwtService.extractRoles(token);
        
        // Assert
        assertThat(roles).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should extract multiple roles from token")
    void should_ExtractMultipleRoles_FromToken() {
        // Arrange 
        Role adminRole = Role.builder()
                .id(2L)
                .name(RoleName.ADMIN)
                .build();
        Role userRole = Role.builder()
                .id(1L)
                .name(RoleName.USER)
                .build();
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        
        User userWithMultipleRoles = User.builder()
                .id(2L)
                .email("admin@example.com")
                .password("password")
                .roles(roles)
                .build();
        
        String token = jwtService.generateAccessToken(userWithMultipleRoles);
        
        // Act
        List<String> extractedRoles = jwtService.extractRoles(token);
        
        // Assert
        assertThat(extractedRoles).containsExactlyInAnyOrder("USER", "ADMIN");
    }
    
    @Test
    @DisplayName("Should return false when token is not expired")
    void should_ReturnFalse_WhenTokenIsNotExpired() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        
        // Act
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // Assert
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should return true when token is expired")
    void should_ReturnTrue_WhenTokenIsExpired() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String expiredToken = jwtService.generateAccessToken(testUser);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        
        // Act
        boolean isExpired = jwtService.isTokenExpired(expiredToken);
        
        // Assert
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Should return true when token is invalid")
    void should_ReturnTrue_WhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalidToken";
        
        // Act
        boolean isExpired = jwtService.isTokenExpired(invalidToken);
        
        // Assert
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Should generate different tokens for access and refresh")
    void should_GenerateDifferentTokens_ForAccessAndRefresh() {
        // Act
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);
        
        // Assert
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Should generate valid tokens on each call")
    void should_GenerateValidTokens_OnEachCall() {
        // Act
        String token1 = jwtService.generateAccessToken(testUser);
        String token2 = jwtService.generateAccessToken(testUser);
        
        assertThat(token1).isNotNull().isNotEmpty();
        assertThat(token2).isNotNull().isNotEmpty();
        
        assertThat(jwtService.extractUsername(token1)).isEqualTo("test@example.com");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("test@example.com");
        
        assertThat(jwtService.isTokenExpired(token1)).isFalse();
        assertThat(jwtService.isTokenExpired(token2)).isFalse();
    
    }
}
