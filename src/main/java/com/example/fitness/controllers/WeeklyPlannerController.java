package com.example.fitness.controllers;

import com.example.fitness.dto.WeeklyPlanRequest;
import com.example.fitness.dto.WeeklyPlanResponse;
import com.example.fitness.services.WeeklyPlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly-planner")
@RequiredArgsConstructor
public class WeeklyPlannerController {

    private final WeeklyPlannerService weeklyPlannerService;

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WeeklyPlanResponse> generate(@RequestBody WeeklyPlanRequest request) {
        return ResponseEntity.ok(weeklyPlannerService.generatePlan(request));
    }
}