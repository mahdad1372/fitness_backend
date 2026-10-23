package com.example.fitness.controllers;

import com.example.fitness.dto.WeeklyPlanRequest;
import com.example.fitness.dto.WeeklyPlanResponse;
import com.example.fitness.services.WeeklyPlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WeeklyPlanResponse> current(@RequestParam Integer studentId) {
        return weeklyPlannerService.findCurrentPlan(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    private LocalDate currentWeekMonday() {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() - 1);
    }
    @PatchMapping("/items/{itemId}/completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> setItemCompleted(
            @PathVariable Integer itemId,
            @RequestParam Integer studentId,
            @RequestParam boolean completed
    ) {
        weeklyPlannerService.setItemCompleted(studentId, itemId, completed);
        return ResponseEntity.noContent().build();
    }
}