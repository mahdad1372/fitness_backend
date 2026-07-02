package com.example.fitness.controllers;
import com.example.fitness.dto.CardiovascularDTO;
import com.example.fitness.dto.CardiovascularResponse;
import com.example.fitness.entitties.Goals;
import com.example.fitness.entitties.Health_metrics;
import com.example.fitness.entitties.User;
import com.example.fitness.services.Complexlogic;
import com.example.fitness.services.Health_metricsService;
import com.example.fitness.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/health_metric")
public class Health_metricsController {
    private final Health_metricsService health_metricsService;
    private final Complexlogic complexlogic;
    private final UserService userservice;
    public Health_metricsController(Health_metricsService health_metricsService, Complexlogic complexlogic,UserService userservice) {
        this.health_metricsService = health_metricsService;
        this.complexlogic = complexlogic;
        this.userservice = userservice;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Health_metrics>> allUsers() {
        return ResponseEntity.ok(health_metricsService.fetchAll());
    }
    @PostMapping("/addmetrics")
    public void addhealth_metrics(@RequestBody Health_metrics health_metrics){
        health_metricsService.addHealth_metrics(health_metrics.getUser_id(),health_metrics.getCholesterol(),
                health_metrics.getBlood_pressure(),health_metrics.getHeart_rate());
    }
    @PostMapping("/calculate/{id}")
    public double calculate(@PathVariable("id") Integer id) {
        return 1.0;
    }
    @GetMapping("/cardiovascular/{id}")
    public CardiovascularResponse  getcardiovascular(@PathVariable("id") Integer id) {
        Integer smoke = userservice.finduserbyid(id).get(0).getSmoke();
        Integer age = userservice.finduserbyid(id).get(0).getAge();
        String gender = userservice.finduserbyid(id).get(0).getGender();
        Float cholesterol = health_metricsService.finduserbyid(id).get(0).getCholesterol();
        Float blood_pressure = health_metricsService.finduserbyid(id).get(0).getBlood_pressure();
        Float heart_rate = health_metricsService.finduserbyid(id).get(0).getHeart_rate();
        Double cardiovascular = health_metricsService.cardiovascular(age,blood_pressure,cholesterol,gender,heart_rate,true);
        String status = health_metricsService.estimateHeartAttackRisk(cardiovascular);
        return new CardiovascularResponse(cardiovascular, status);
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<Health_metrics>> getUserById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(health_metricsService.finduserbyid(id));
    }
    @GetMapping("/findbyhealth_id/{id}")
    public ResponseEntity<List<Health_metrics>> getGoalsById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(health_metricsService.getGoalsById(id));
    }
    @DeleteMapping("/deletehealth/{id}")
    public void deletehealthbyid(@PathVariable("id") Integer id) {
        health_metricsService.deleteHealthById(id);
    }
    @PutMapping("/update_healthmetric/{id}")
    public ResponseEntity<String> updateFood(
            @PathVariable("id") Integer id, @RequestBody Health_metrics health_metrics
    ) {
        health_metricsService.updateHealth_metrics(
                id,health_metrics.getCholesterol(),health_metrics.getBlood_pressure(),health_metrics.getHeart_rate());

        return ResponseEntity.ok("Health updated successfully");
    }

}
