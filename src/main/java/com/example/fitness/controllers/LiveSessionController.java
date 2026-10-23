package com.example.fitness.controllers;

import com.example.fitness.dto.*;
import com.example.fitness.entitties.LiveSession;
import com.example.fitness.repositories.LiveSessionRepository;
import com.example.fitness.services.VirtualSensorEngine;
import com.example.fitness.services.WeeklyPlanPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/live-sessions")
public class LiveSessionController {

    @Autowired
    private LiveSessionRepository liveSessionRepository;

    @Autowired
    private VirtualSensorEngine virtualSensorEngine;

    @Autowired
    private WeeklyPlanPersistenceService weeklyPlanPersistenceService;

    private final Random random = new Random();

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StartSessionRequestDto body) {
        PreSessionCheckDto check = simulatePreSessionCheck();
        if (!check.isClearedToStart()) {
            return ResponseEntity.ok(new StartSessionResponseDto(null, check));
        }

        LiveSession session = new LiveSession();
        session.setItem_id(body.getItemId());
        session.setStudent_id(body.getStudentId());
        session.setExercise_name(body.getExerciseName());
        session.setDuration_minutes(body.getDurationMinutes() != null ? body.getDurationMinutes() : 20);
        session.setDifficulty_level(body.getDifficultyLevel() != null ? body.getDifficultyLevel() : 5);
        session.setTrack_steps("cardio".equalsIgnoreCase(body.getMuscleGroup()));
        session.setStatus("IN_PROGRESS");
        session.setPre_session_systolic(check.getSystolic());
        session.setPre_session_diastolic(check.getDiastolic());

        LiveSession saved = liveSessionRepository.save(session);
        virtualSensorEngine.startSession(saved.getId(), saved);

        return ResponseEntity.ok(new StartSessionResponseDto(saved.getId(), check));
    }

    @GetMapping("/{id}/reading")
    public ResponseEntity<?> reading(@PathVariable Long id, @RequestParam Integer studentId) {
        Optional<LiveSession> session = liveSessionRepository.findByIdForStudent(id, studentId);
        if (session.isEmpty()) {
            return ResponseEntity.status(404).body("No live session with that id for this student.");
        }
        Optional<SensorReadingDto> reading = virtualSensorEngine.currentReading(id);
        if (reading.isEmpty()) {
            return ResponseEntity.status(404).body("This session has already finished.");
        }
        return ResponseEntity.ok(reading.get());
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<?> finish(@PathVariable Long id, @RequestParam Integer studentId) {
        Optional<LiveSession> sessionOpt = liveSessionRepository.findByIdForStudent(id, studentId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(404).body("No live session with that id for this student.");
        }
        LiveSession session = sessionOpt.get();
        session.setStatus("COMPLETED");
        session.setFinished_at(new Date());
        liveSessionRepository.save(session);
        virtualSensorEngine.finishSession(id);

        weeklyPlanPersistenceService.setItemCompleted(session.getStudent_id(), Math.toIntExact(session.getItem_id()), true);

        return ResponseEntity.ok().build();
    }

    private PreSessionCheckDto simulatePreSessionCheck() {
        int systolic = 110 + random.nextInt(20);
        int diastolic = 70 + random.nextInt(14);
        boolean cleared = systolic < 140 && diastolic < 90;
        return new PreSessionCheckDto(systolic, diastolic, cleared);
    }
}