package org.example.fitnesstracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitnesstracker.controller.docs.AnalyticsControllerApi;
import org.example.fitnesstracker.dto.response.analytics.AnalyticsResponse;
import org.example.fitnesstracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController implements AnalyticsControllerApi {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Override
    public ResponseEntity<AnalyticsResponse> getAnalytics(
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo
    ) {
        log.debug("Getting analytics with dateFrom: {}, dateTo: {}", dateFrom, dateTo);
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }

        return ResponseEntity.ok(analyticsService.getAnalytics(dateFrom, dateTo));
    }
}
