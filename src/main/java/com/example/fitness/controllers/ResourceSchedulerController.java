package com.example.fitness.controllers;

import com.example.fitness.dto.OptimizeScheduleResponse;
import com.example.fitness.services.ResourceSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource-scheduler")
@RequiredArgsConstructor
public class ResourceSchedulerController {

    private final ResourceSchedulerService resourceSchedulerService;

    // Runs the exact Branch & Bound optimizer over every currently PENDING
    // session request, accepting/rejecting all of them in one pass. Any
    // authenticated user can trigger it for now (matching the isAuthenticated()
    // convention already used by MealPlannerController/WeeklyPlannerController)
    // - a coach-only restriction would be a manual role check here once the
    // frontend distinguishes "my queue" from "everyone's queue".
    @PostMapping("/optimize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptimizeScheduleResponse> optimize() {
        return ResponseEntity.ok(resourceSchedulerService.optimize());
    }
}