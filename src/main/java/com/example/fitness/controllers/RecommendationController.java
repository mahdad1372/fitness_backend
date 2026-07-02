package com.example.fitness.controllers;

import com.example.fitness.services.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getRecommendations(
            @PathVariable Integer userId) {
        String recommendation = recommendationService
                .getRecommendationsForUser(userId);
        return ResponseEntity.ok(Map.of("recommendation", recommendation));
    }
}