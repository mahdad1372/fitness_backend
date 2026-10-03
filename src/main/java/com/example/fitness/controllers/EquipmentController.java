package com.example.fitness.controllers;

import com.example.fitness.dto.EquipmentDto;
import com.example.fitness.dto.EquipmentQueueEntryDto;
import com.example.fitness.dto.EquipmentStatusDto;
import com.example.fitness.services.EquipmentService;
import com.example.fitness.services.EquipmentUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EquipmentUsageService equipmentUsageService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentDto>> listAll() {
        return ResponseEntity.ok(equipmentService.listAll());
    }

    // Configuring how many units of a machine the studio owns is a
    // gym-management action, so it's gated the same way other management
    // CRUD is elsewhere in this app (hasRole('ADMIN')).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentDto> create(@RequestBody EquipmentDto request) {
        return ResponseEntity.ok(equipmentService.create(request.getName(), request.getCapacity()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable("id") Integer id) {
        equipmentService.delete(id);
    }

    // ==========================
    // Live Equipment Board - walk-up check-in/out + queue, mirroring the
    // Chat feature's capacity + waiting-room + notification pattern.
    // ==========================

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentStatusDto>> status(@RequestParam("userId") Integer userId) {
        return ResponseEntity.ok(equipmentUsageService.status(userId));
    }

    @PostMapping("/{id}/checkin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> checkIn(
            @PathVariable("id") Integer equipmentId,
            @RequestBody Map<String, Integer> request
    ) {
        try {
            equipmentUsageService.checkIn(equipmentId, request.get("userId"));
            return ResponseEntity.ok("Checked in");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> checkOut(
            @PathVariable("id") Integer equipmentId,
            @RequestBody Map<String, Integer> request
    ) {
        try {
            equipmentUsageService.checkOut(equipmentId, request.get("userId"));
            return ResponseEntity.ok("Checked out");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/queue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> joinQueue(
            @PathVariable("id") Integer equipmentId,
            @RequestBody Map<String, Integer> request
    ) {
        try {
            equipmentUsageService.joinQueue(equipmentId, request.get("userId"));
            return ResponseEntity.ok("Joined queue");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/queue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentQueueEntryDto>> queue(@PathVariable("id") Integer equipmentId) {
        return ResponseEntity.ok(equipmentUsageService.queueFor(equipmentId));
    }

    @DeleteMapping("/{id}/queue/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> leaveQueue(
            @PathVariable("id") Integer equipmentId,
            @PathVariable("userId") Integer userId
    ) {
        equipmentUsageService.leaveQueue(equipmentId, userId);
        return ResponseEntity.ok("Left queue");
    }
}