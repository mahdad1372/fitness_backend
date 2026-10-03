package com.example.fitness.controllers;

import com.example.fitness.dto.MealPlanRequest;
import com.example.fitness.dto.MealPlanResponse;
import com.example.fitness.services.MealPlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meal-planner")
@RequiredArgsConstructor
public class MealPlannerController {

    private final MealPlannerService mealPlannerService;

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MealPlanResponse> generate(@RequestBody MealPlanRequest request) {
        return ResponseEntity.ok(mealPlannerService.generatePlan(request));
    }
}