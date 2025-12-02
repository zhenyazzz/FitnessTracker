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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        @Valid @RequestBody MediaFilterDto filter,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MediaResponse> result = mediaService.getAllMedia(filter, pageable);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable Long id) {
        MediaResponse result = mediaService.getMediaById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<MediaResponse> createMedia(
        @RequestPart(value = "note", required = false) String note,
        @RequestPart("file") @NotNull(message = "File is required") MultipartFile file) {
        
        MediaRequest request = new MediaRequest(note);
        MediaResponse result = mediaService.createMedia(request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

}
