package org.example.fitnesstracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitnesstracker.controller.docs.MediaControllerApi;
import org.example.fitnesstracker.dto.request.media.MediaFilterDto;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.service.MediaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController implements MediaControllerApi {
    private final MediaService mediaService;

    @GetMapping
    @Override
    public ResponseEntity<Page<MediaResponse>> getAllMedia(
        @Valid MediaFilterDto filter,
        @Valid Pageable pageable
    ) {
        log.debug("Request to get all media with filters: {}", filter);
        Page<MediaResponse> result = mediaService.getAllMedia(filter, pageable);
        log.info("Successfully retrieved {} media items (page {}, total: {})", 
            result.getContent().size(), pageable.getPageNumber(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable Long id) {
        log.debug("Request to get media by id: {}", id);
        MediaResponse result = mediaService.getMediaById(id);
        log.info("Successfully retrieved media with id: {}", id);
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<MediaResponse> createMedia(
        @RequestPart(value = "note", required = false) String note,
        @RequestPart("file") @NotNull(message = "File is required") MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }
        
        if (note != null && note.length() > 500) {
            throw new IllegalArgumentException("Note cannot exceed 500 characters");
        }
        
        MediaRequest request = new MediaRequest(note);
        log.debug("Request to create media: filename={}, size={}, contentType={}, note={}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType(), note);
        MediaResponse result = mediaService.createMedia(request, file);
        log.info("Successfully created media with id: {}, filename: {}", 
            result.id(), file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        log.debug("Request to delete media with id: {}", id);
        mediaService.deleteMedia(id);
        log.info("Successfully deleted media with id: {}", id);
        return ResponseEntity.noContent().build();
    }

}
