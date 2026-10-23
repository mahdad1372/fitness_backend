package com.example.fitness.controllers;

import com.example.fitness.dto.CoachSummaryDto;
import com.example.fitness.dto.MyCoachDto;
import com.example.fitness.services.CoachSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Subscribing/changing a coach does NOT go through this controller - the
// frontend calls the EXISTING /cart/add and /cart/updatecart/{id} endpoints
// directly (see CartController), writing a "PRIVATE COACH" row. This
// controller only reads.
@RestController
@RequiredArgsConstructor
public class CoachSubscriptionController {

    private final CoachSubscriptionService coachSubscriptionService;

    @GetMapping("/api/coaches")
    public ResponseEntity<List<CoachSummaryDto>> listCoaches() {
        return ResponseEntity.ok(coachSubscriptionService.listCoaches());
    }

    @GetMapping("/api/students/{studentId}/coach")
    public ResponseEntity<MyCoachDto> myCoach(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.ok(coachSubscriptionService.getMyCoach(studentId));
    }
}