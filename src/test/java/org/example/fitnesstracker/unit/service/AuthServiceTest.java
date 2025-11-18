package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.dto.request.auth.LoginRequest;
import org.example.fitnesstracker.dto.request.auth.LogoutRequest;
import org.example.fitnesstracker.dto.request.auth.RefreshTokenRequest;
import org.example.fitnesstracker.dto.request.auth.RegisterRequest;
import org.example.fitnesstracker.dto.response.AuthResponse;
import org.example.fitnesstracker.exception.EmailAlreadyExistsException;
import org.example.fitnesstracker.exception.RefreshTokenExpiredException;
import org.example.fitnesstracker.exception.RefreshTokenNotFoundException;
import org.example.fitnesstracker.model.Role;
import org.example.fitnesstracker.model.RefreshToken;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.enums.RoleName;
import org.example.fitnesstracker.repository.RefreshTokenRepository;
import org.example.fitnesstracker.repository.RoleRepository;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.security.JwtService;
import org.example.fitnesstracker.service.AuthService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;

    // Общие тестовые данные
    private static User testUser;
    private static RefreshToken testRefreshToken;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_USERNAME = "username";
    private static final String TEST_REFRESH_TOKEN = "refreshToken";

    @BeforeAll
    static void setUp() {
        testUser = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .password("encodedPassword")
                .username(TEST_USERNAME)
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(TEST_REFRESH_TOKEN)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void should_RegisterUser_Successfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "username");
        
        User savedUser = User.builder()
            .id(1L)
            .email(request.email())
            .password("encodedPassword")
            .username(request.username())
            .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(Role.builder().name(RoleName.USER).build()));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(jwtService.extractExpirationLocal("refreshToken")).thenReturn(LocalDateTime.now().plusDays(7));

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(request.password());
        verify(jwtService).generateAccessToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
        verify(jwtService).extractExpirationLocal("refreshToken");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(roleRepository).findByName(RoleName.USER);
        verify(roleRepository, never()).findByName(RoleName.ADMIN);
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when user with email already exists")
    void should_ThrowEmailAlreadyExistsException_When_UserWithEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "username");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act + Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login user successfully")
    void should_LoginUser_Successfully() {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(jwtService.extractExpirationLocal("refreshToken"))
            .thenReturn(LocalDateTime.now().plusDays(7));


        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), testUser.getPassword());
        verify(jwtService).generateAccessToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).extractExpirationLocal("refreshToken");
        verify(refreshTokenRepository).deleteByUser(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user with email not found")
    void should_ThrowUsernameNotFoundException_When_UserWithEmailNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password");
        
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken(any(User.class));
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password is incorrect")
    void should_ThrowBadCredentialsException_When_PasswordIsIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.password(), testUser.getPassword())).thenReturn(false);

        // Act + Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(userRepository).findByEmail(request.email());
        verify(passwordEncoder).matches(request.password(), testUser.getPassword());
        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken(any(User.class));
        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }



    @Test
    @DisplayName("Should refresh token successfully when token is valid")
    void should_RefreshToken_Successfully_When_TokenIsValid() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(testRefreshToken));
        when(jwtService.isTokenExpired(TEST_REFRESH_TOKEN)).thenReturn(false);
        when(jwtService.generateRefreshToken(testUser)).thenReturn("newRefreshToken");
        when(jwtService.generateAccessToken(testUser)).thenReturn("newAccessToken");
        when(jwtService.extractExpirationLocal("newRefreshToken")).thenReturn(LocalDateTime.now().plusDays(7));

        // Act
        AuthResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("newAccessToken");
        assertThat(response.refreshToken()).isEqualTo("newRefreshToken");

        verify(refreshTokenRepository).findByToken(TEST_REFRESH_TOKEN);
        verify(jwtService).isTokenExpired(TEST_REFRESH_TOKEN);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(jwtService).extractExpirationLocal("newRefreshToken");
        verify(refreshTokenRepository).save(testRefreshToken);
    }

    @Test
    @DisplayName("Should throw RefreshTokenNotFoundException when refresh token not found")
    void should_ThrowRefreshTokenNotFoundException_When_RefreshTokenNotFound() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("nonExistentToken");

        when(refreshTokenRepository.findByToken("nonExistentToken")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(RefreshTokenNotFoundException.class, () -> authService.refreshToken(request));

        verify(refreshTokenRepository).findByToken("nonExistentToken");
        verify(jwtService, never()).isTokenExpired(anyString());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw RefreshTokenExpiredException when refresh token is expired")
    void should_ThrowRefreshTokenExpiredException_When_RefreshTokenIsExpired() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(testRefreshToken));
        when(jwtService.isTokenExpired(TEST_REFRESH_TOKEN)).thenReturn(true);

        // Act + Assert
        assertThrows(RefreshTokenExpiredException.class, () -> authService.refreshToken(request));

        verify(refreshTokenRepository).findByToken(TEST_REFRESH_TOKEN);
        verify(jwtService).isTokenExpired(TEST_REFRESH_TOKEN);
        verify(refreshTokenRepository).delete(testRefreshToken);
        verify(jwtService, never()).generateRefreshToken(any(User.class));
        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should logout user successfully")
    void should_LogoutUser_Successfully() {
        // Arrange
        LogoutRequest request = new LogoutRequest(TEST_REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN)).thenReturn(Optional.of(testRefreshToken));

        // Act
        authService.logout(request);

        // Assert
        verify(refreshTokenRepository).findByToken(TEST_REFRESH_TOKEN);
        verify(refreshTokenRepository).delete(testRefreshToken);
    }

    @Test
    @DisplayName("Should throw RefreshTokenNotFoundException when logout token not found")
    void should_ThrowRefreshTokenNotFoundException_When_LogoutTokenNotFound() {
        // Arrange
        LogoutRequest request = new LogoutRequest("nonExistentToken");
        
        when(refreshTokenRepository.findByToken("nonExistentToken")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(RefreshTokenNotFoundException.class, () -> authService.logout(request));

        verify(refreshTokenRepository).findByToken("nonExistentToken");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

}

