package org.example.fitnesstracker.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.service.MediaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping
    public ResponseEntity<Page<MediaResponse>> getAllMedia(
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDirection,
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size

    ) {
        return ResponseEntity.ok(mediaService.getAllMedia(sortBy, sortDirection, dateFrom, dateTo, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    @PostMapping
    public ResponseEntity<MediaResponse> createMedia(@RequestBody MediaRequest request) {
        return ResponseEntity.ok(mediaService.createMedia(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

}
