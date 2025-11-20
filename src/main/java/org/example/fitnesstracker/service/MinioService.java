package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.example.fitnesstracker.exception.MediaUploadException;
import org.example.fitnesstracker.exception.MediaDeleteException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {


    private static final int PRESIGNED_URL_EXPIRATION_HOURS = 24;

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    public void init() {
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());

            if (!exists) {
                log.info("Bucket '{}' does not exist. Creating it...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
                
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.debug("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new MediaUploadException("Failed to initialize MinIO bucket: " + e.getMessage());
        }
    }


    public String uploadFile(MultipartFile file) {


        String objectPath = generateObjectName(file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            log.debug("Uploading file '{}' to path '{}'", 
                file.getOriginalFilename(), objectPath);

            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

            log.info("File '{}' uploaded successfully to path '{}'", 
                file.getOriginalFilename(), objectPath);
            
            return objectPath;
        } catch (MinioException e) {
            log.error("MinIO error while uploading file '{}': {}", 
                file.getOriginalFilename(), e.getMessage(), e);
            throw new MediaUploadException(
                "Failed to upload file to MinIO: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while uploading file '{}': {}", 
                file.getOriginalFilename(), e.getMessage(), e);
            throw new MediaUploadException(
                "Failed to upload file: " + e.getMessage());
        }
    }


    public void deleteFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            log.warn("Attempt to delete file with empty path");
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try {
            log.debug("Deleting file '{}'", path);

            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());

            log.info("File '{}' deleted successfully", path);
        } catch (MinioException e) {
            log.error("MinIO error while deleting file '{}': {}", 
                path, e.getMessage(), e);
            throw new MediaDeleteException(
                "Failed to delete file from MinIO: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while deleting file '{}': {}", 
                path, e.getMessage(), e);
            throw new MediaDeleteException(
                "Failed to delete file from MinIO: " + e.getMessage());
        }
    }

    public String generateFileUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try {
            log.debug("Generating presigned URL for file '{}'", path);

            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(path)
                    .expiry(PRESIGNED_URL_EXPIRATION_HOURS, TimeUnit.HOURS)
                    .build()
            );

            log.debug("Presigned URL generated successfully for file '{}'", path);
            return url;
        } catch (MinioException e) {
            log.error("MinIO error while generating presigned URL for file '{}': {}", 
                path, e.getMessage(), e);
            throw new MediaUploadException(
                "Failed to generate file URL: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while generating presigned URL for file '{}': {}", 
                path, e.getMessage(), e);
            throw new MediaUploadException(
                "Failed to generate file URL: " + e.getMessage());
        }
    }

    private String generateObjectName(String filename) {
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
