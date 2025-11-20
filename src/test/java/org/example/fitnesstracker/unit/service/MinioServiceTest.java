package org.example.fitnesstracker.unit.service;

import org.example.fitnesstracker.exception.MediaDeleteException;
import org.example.fitnesstracker.exception.MediaUploadException;
import org.example.fitnesstracker.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinioService Unit Tests")
class MinioServiceTest {

    @InjectMocks
    private MinioService minioService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MultipartFile multipartFile;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String TEST_FILENAME = "test-image.jpg";
    private static final String TEST_CONTENT_TYPE = "image/jpeg";
    private static final long TEST_FILE_SIZE = 1024L;
    private static final String TEST_OBJECT_PATH = "test-object-path.jpg";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("Should create bucket when it does not exist")
    void should_CreateBucket_WhenItDoesNotExist() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));

        // Act
        ReflectionTestUtils.invokeMethod(minioService, "ensureBucketExists");

        // Assert
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Should not create bucket when it already exists")
    void should_NotCreateBucket_WhenItAlreadyExists() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        ReflectionTestUtils.invokeMethod(minioService, "ensureBucketExists");

        // Assert
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Should throw MediaUploadException when bucket check fails")
    void should_ThrowMediaUploadException_WhenBucketCheckFails() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
            .thenThrow(new RuntimeException("Bucket check failed"));

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(minioService, "ensureBucketExists"))
            .isInstanceOf(MediaUploadException.class)
            .hasMessageContaining("Failed to initialize MinIO bucket");
    }

    @Test
    @DisplayName("Should upload file successfully")
    void should_UploadFile_Successfully() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE);
        when(multipartFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // Act
        String result = minioService.uploadFile(multipartFile);

        // Assert
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).endsWith(".jpg");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should upload file without extension")
    void should_UploadFile_WithoutExtension() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        when(multipartFile.getOriginalFilename()).thenReturn("test-file");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE);
        when(multipartFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // Act
        String result = minioService.uploadFile(multipartFile);

        // Assert
        assertThat(result).isNotNull().isNotEmpty();
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw MediaUploadException when MinIO upload fails")
    void should_ThrowMediaUploadException_WhenMinIOUploadFails() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(TEST_FILE_SIZE);
        when(multipartFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        when(minioClient.putObject(any(PutObjectArgs.class)))
            .thenThrow(new RuntimeException("Upload failed"));

        // Act & Assert
        assertThatThrownBy(() -> minioService.uploadFile(multipartFile))
            .isInstanceOf(MediaUploadException.class)
            .hasMessageContaining("Failed to upload file");
    }

    @Test
    @DisplayName("Should delete file successfully")
    void should_DeleteFile_Successfully() throws Exception {
        // Arrange
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act
        minioService.deleteFile(TEST_OBJECT_PATH);

        // Assert
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when path is null")
    void should_ThrowIllegalArgumentException_WhenPathIsNull() throws Exception {
        // Act & Assert
        assertThatThrownBy(() -> minioService.deleteFile(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null or empty");
        
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when path is empty")
    void should_ThrowIllegalArgumentException_WhenPathIsEmpty() throws Exception {
        // Act & Assert
        assertThatThrownBy(() -> minioService.deleteFile(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null or empty");
        
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when path is blank")
    void should_ThrowIllegalArgumentException_WhenPathIsBlank() throws Exception {
        // Act & Assert
        assertThatThrownBy(() -> minioService.deleteFile("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null or empty");
        
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw MediaDeleteException when MinIO delete fails")
    void should_ThrowMediaDeleteException_WhenMinIODeleteFails() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Delete failed"))
            .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // Act & Assert
        assertThatThrownBy(() -> minioService.deleteFile(TEST_OBJECT_PATH))
            .isInstanceOf(MediaDeleteException.class)
            .hasMessageContaining("Failed to delete file from MinIO");
    }


    @Test
    @DisplayName("Should generate presigned URL successfully")
    void should_GeneratePresignedUrl_Successfully() throws Exception {
        // Arrange
        String expectedUrl = "http://localhost:9000/test-bucket/test-object.jpg?signature=abc123";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
            .thenReturn(expectedUrl);

        // Act
        String result = minioService.generateFileUrl(TEST_OBJECT_PATH);

        // Assert
        assertThat(result).isEqualTo(expectedUrl);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when path is null for URL generation")
    void should_ThrowIllegalArgumentException_WhenPathIsNullForUrlGeneration() throws Exception {
        // Act & Assert
        assertThatThrownBy(() -> minioService.generateFileUrl(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null or empty");
        
        verify(minioClient, never()).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when path is empty for URL generation")
    void should_ThrowIllegalArgumentException_WhenPathIsEmptyForUrlGeneration() throws Exception {
        // Act & Assert
        assertThatThrownBy(() -> minioService.generateFileUrl(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null or empty");
        
        verify(minioClient, never()).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Should throw MediaUploadException when MinIO URL generation fails")
    void should_ThrowMediaUploadException_WhenMinIOUrlGenerationFails() throws Exception {
        // Arrange
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
            .thenThrow(new RuntimeException("URL generation failed"));

        // Act & Assert
        assertThatThrownBy(() -> minioService.generateFileUrl(TEST_OBJECT_PATH))
            .isInstanceOf(MediaUploadException.class)
            .hasMessageContaining("Failed to generate file URL");
    }

   

}
