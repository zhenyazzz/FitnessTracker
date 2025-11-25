package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.service.MediaService;
import org.example.fitnesstracker.repository.MediaRepository;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.request.media.MediaFilterDto;
import org.example.fitnesstracker.dto.request.DateFilterDto;
import org.example.fitnesstracker.mapper.MediaMapper;
import org.example.fitnesstracker.security.SecurityUtils;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.service.MinioService;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.exception.AccessDeniedException;
import org.example.fitnesstracker.exception.MediaNotFoundException;
import org.example.fitnesstracker.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MediaService Unit Tests")
class MediaServiceTest {
    @InjectMocks
    private MediaService mediaService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private MultipartFile multipartFile;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private User testUser;
    private Media testMedia;
    private MediaRequest testMediaRequest;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_MEDIA_ID = 1L;
    private static final String TEST_FILE_PATH = "uuid-generated-path.jpg";
    private static final String TEST_FILE_URL = "http://localhost:9000/test-bucket/uuid-generated-path.jpg";
    private static final long TEST_FILE_SIZE = 1024L;
    private static final String TEST_CONTENT_TYPE = "image/jpeg";

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
        
        testUser = User.builder()
            .id(TEST_USER_ID)
            .email("test@example.com")
            .username("testuser")
            .build();

        testMedia = Media.builder()
            .id(TEST_MEDIA_ID)
            .user(testUser)
            .path(TEST_FILE_PATH)
            .note("test note")
            .fileSize(TEST_FILE_SIZE)
            .mimeType(TEST_CONTENT_TYPE)
            .createdAt(LocalDateTime.now())
            .build();

        testMediaRequest = new MediaRequest("test note");
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityUtils != null) {
            mockedSecurityUtils.close();
        }
    }

    @Test
    @DisplayName("Should get all media successfully")
    void should_GetAllMedia_Successfully() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Media> mediaPage = new PageImpl<>(List.of(testMedia), pageable, 1);
        
        MediaResponse mediaResponse = new MediaResponse(
            TEST_MEDIA_ID,
            TEST_FILE_URL,
            "test note",
            TEST_FILE_SIZE,
            TEST_CONTENT_TYPE,
            testMedia.getCreatedAt()
        );

        when(mediaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mediaPage);
        when(minioService.generateFileUrl(TEST_FILE_PATH)).thenReturn(TEST_FILE_URL);
        when(mediaMapper.toResponse(testMedia, TEST_FILE_URL)).thenReturn(mediaResponse);

        // Act
        Page<MediaResponse> result = mediaService.getAllMedia(
            new MediaFilterDto(new DateFilterDto(null, null)), pageable
        );
        

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        
        MediaResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(TEST_MEDIA_ID);
        assertThat(response.presignedUrl()).isEqualTo(TEST_FILE_URL);
        assertThat(response.note()).isEqualTo("test note");
        assertThat(response.fileSize()).isEqualTo(TEST_FILE_SIZE);
        assertThat(response.mimeType()).isEqualTo(TEST_CONTENT_TYPE);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(mediaRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(minioService).generateFileUrl(TEST_FILE_PATH);
        verify(mediaMapper).toResponse(testMedia, TEST_FILE_URL);
    }

    @Test
    @DisplayName("Should get media by id successfully")
    void should_GetMediaById_Successfully() throws Exception {
        // Arrange
        String expectedUrl = "http://localhost:9000/test-bucket/test.jpg";
        MediaResponse mediaResponse = new MediaResponse(
            TEST_MEDIA_ID,
            expectedUrl,
            "test note",
            100L,
            "image/jpeg",
            LocalDateTime.now()
        );
        
        when(mediaRepository.findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID))
            .thenReturn(Optional.of(testMedia));
        when(minioService.generateFileUrl(testMedia.getPath())).thenReturn(expectedUrl);
        when(mediaMapper.toResponse(testMedia, expectedUrl)).thenReturn(mediaResponse);

        // Act
        MediaResponse result = mediaService.getMediaById(TEST_MEDIA_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_MEDIA_ID);
        assertThat(result.presignedUrl()).isEqualTo(expectedUrl);
        assertThat(result.note()).isEqualTo("test note");
        assertThat(result.fileSize()).isEqualTo(100L);
        assertThat(result.mimeType()).isEqualTo("image/jpeg");
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(mediaRepository).findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID);
        verify(minioService).generateFileUrl(testMedia.getPath());
        verify(mediaMapper).toResponse(testMedia, expectedUrl);
    }
    
    @Test
    @DisplayName("Should throw MediaNotFoundException when media not found")
    void should_ThrowMediaNotFoundException_WhenMediaNotFound() throws Exception {
        // Arrange
        when(mediaRepository.findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> mediaService.getMediaById(TEST_MEDIA_ID))
            .isInstanceOf(MediaNotFoundException.class)
            .hasMessageContaining("Media not found with id: " + TEST_MEDIA_ID);
        
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(mediaRepository).findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID);
        verify(minioService, never()).generateFileUrl(any());
        verify(mediaMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("Should create media successfully")
    void should_CreateMedia_Successfully() throws Exception {
        // Arrange
        
        MediaResponse expectedResponse = new MediaResponse(
            TEST_MEDIA_ID,
            TEST_FILE_URL,
            "test note",
            TEST_FILE_SIZE,
            TEST_CONTENT_TYPE,
            LocalDateTime.now()
        );

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE);
        when(multipartFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        when(minioService.uploadFile(multipartFile)).thenReturn(TEST_FILE_PATH);
        when(mediaMapper.toEntity(testMediaRequest, testUser, TEST_FILE_PATH, TEST_FILE_SIZE, TEST_CONTENT_TYPE))
            .thenReturn(testMedia);
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(minioService.generateFileUrl(TEST_FILE_PATH)).thenReturn(TEST_FILE_URL);
        when(mediaMapper.toResponse(testMedia, TEST_FILE_URL)).thenReturn(expectedResponse);

        // Act
        MediaResponse result = mediaService.createMedia(testMediaRequest, multipartFile);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_MEDIA_ID);
        assertThat(result.presignedUrl()).isEqualTo(TEST_FILE_URL);
        assertThat(result.note()).isEqualTo("test note");
        assertThat(result.fileSize()).isEqualTo(TEST_FILE_SIZE);
        assertThat(result.mimeType()).isEqualTo(TEST_CONTENT_TYPE);

        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(multipartFile).isEmpty();
        verify(minioService).uploadFile(multipartFile);
        verify(mediaMapper).toEntity(testMediaRequest, testUser, TEST_FILE_PATH, TEST_FILE_SIZE, TEST_CONTENT_TYPE);
        verify(mediaRepository).save(any(Media.class));
        verify(minioService).generateFileUrl(TEST_FILE_PATH);
        verify(mediaMapper).toResponse(testMedia, TEST_FILE_URL);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when file is empty")
    void should_ThrowIllegalArgumentException_WhenFileIsEmpty() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> mediaService.createMedia(testMediaRequest, multipartFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File is empty");

        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(multipartFile).isEmpty();
        verify(minioService, never()).uploadFile(any());
        verify(mediaMapper, never()).toEntity(any(), any(), any(), any(), any());
        verify(mediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void should_ThrowUserNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> mediaService.createMedia(testMediaRequest, multipartFile))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with id: " + TEST_USER_ID);

        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(userRepository).findById(TEST_USER_ID);
        verify(multipartFile, never()).isEmpty();
        verify(minioService, never()).uploadFile(any());
        verify(mediaMapper, never()).toEntity(any(), any(), any(), any(), any());
        verify(mediaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete media successfully")
    void should_DeleteMedia_Successfully() throws Exception {
        // Arrange
        when(mediaRepository.findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID)).thenReturn(Optional.of(testMedia));

        // Act
        mediaService.deleteMedia(TEST_MEDIA_ID);

        // Assert
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(mediaRepository).findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID);
        verify(minioService).deleteFile(testMedia.getPath());
        verify(mediaRepository).delete(testMedia);
    }

    @Test
    @DisplayName("Should throw MediaNotFoundException when media not found")
    void should_ThrowMediaNotFoundException_WhenMediaNotFoundToDelete() throws Exception {
        // Arrange
        when(mediaRepository.findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> mediaService.deleteMedia(TEST_MEDIA_ID))
            .isInstanceOf(MediaNotFoundException.class)
            .hasMessageContaining("Media not found with id: " + TEST_MEDIA_ID);

        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId);
        verify(mediaRepository).findByIdAndUserId(TEST_MEDIA_ID, TEST_USER_ID);
        verify(minioService, never()).deleteFile(any());
        verify(mediaRepository, never()).delete(any(Media.class));
    }

    
}
