package com.example.fitness.controllers;

import com.example.fitness.dto.CoachSummaryDto;
import com.example.fitness.dto.CreateSessionRequestDto;
import com.example.fitness.dto.SessionRequestDto;
import com.example.fitness.services.SessionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-requests")
@RequiredArgsConstructor
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionRequestDto> create(@RequestBody CreateSessionRequestDto request) {
        return ResponseEntity.ok(sessionRequestService.create(request));
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionRequestDto>> pending() {
        return ResponseEntity.ok(sessionRequestService.listByStatus("PENDING"));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionRequestDto>> byCoach(@PathVariable("coachId") Integer coachId) {
        return ResponseEntity.ok(sessionRequestService.listByCoach(coachId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionRequestDto>> byStudent(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.ok(sessionRequestService.listByStudent(studentId));
    }

    @GetMapping("/coaches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CoachSummaryDto>> coaches() {
        return ResponseEntity.ok(sessionRequestService.listCoaches());
    }
}