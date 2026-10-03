package com.example.fitness.controllers;

import com.example.fitness.entitties.Exercise;
import com.example.fitness.graph.ExerciseGraph;
import com.example.fitness.services.ExerciseGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@RequestMapping("/api/exercises")
@RestController
@RequiredArgsConstructor
public class ExerciseGraphController {

    private final ExerciseGraphService graphService;

    @GetMapping("/{id}/substitutes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Exercise>> substitutes(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "") Set<String> unavailableEquipment,
            @RequestParam(required = false, defaultValue = "5") int maxResults) {
        return ResponseEntity.ok(graphService.findSubstitutes(id, unavailableEquipment, maxResults));
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Exercise>> allExercises() {
        return ResponseEntity.ok(graphService.getAllExercises());
    }
    @GetMapping("/progression")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Exercise>> progression(@RequestParam String muscleGroup) {
        return ResponseEntity.ok(graphService.getProgressionOrder(muscleGroup));
    }

    @GetMapping("/path")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> smoothestPath(@RequestParam Integer from, @RequestParam Integer to) {
        Optional<ExerciseGraph.PathResult> result = graphService.findSmoothestPath(from, to);
        return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}