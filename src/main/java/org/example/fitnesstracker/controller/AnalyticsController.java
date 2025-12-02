package org.example.fitnesstracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitnesstracker.controller.docs.AnalyticsControllerApi;
import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController implements AnalyticsControllerApi {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Override
    public ResponseEntity<AnalyticsResponse> getAnalytics(
        @Valid @RequestBody AnalyticsRequest request) {
    
        return ResponseEntity.ok(analyticsService.getAnalytics(request));
    }
}
