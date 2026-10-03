package com.example.fitness.controllers;
import com.example.fitness.dto.CardiovascularAssessmentRequestDto;
import com.example.fitness.dto.CardiovascularAssessmentResponseDto;
import com.example.fitness.dto.CardiovascularResponse;
import com.example.fitness.entitties.Goals;
import com.example.fitness.entitties.Health_metrics;
import com.example.fitness.services.CardiovascularRiskService;
import com.example.fitness.entitties.User;
import com.example.fitness.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health_metric")
public class Health_metricsController {
    private final Health_metricsService health_metricsService;
    private final Complexlogic complexlogic;
    private final UserService userservice;
    private final GoogleFitDataService googleFitDataService;
    private final CardiovascularRiskService cardiovascularRiskService;
    public Health_metricsController(Health_metricsService health_metricsService, Complexlogic complexlogic, UserService userservice, GoogleFitDataService googleFitDataService, CardiovascularRiskService cardiovascularRiskService) {
        this.health_metricsService = health_metricsService;
        this.complexlogic = complexlogic;
        this.userservice = userservice;
        this.googleFitDataService = googleFitDataService;
        this.cardiovascularRiskService = cardiovascularRiskService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Health_metrics>> allUsers() {
        return ResponseEntity.ok(health_metricsService.fetchAll());
    }
    @PostMapping("/cardiovascular/{id}")
    public ResponseEntity<Object> getcardiovascular(@PathVariable("id") Integer id,
                                                    @RequestBody CardiovascularAssessmentRequestDto request) {
        try {
            CardiovascularAssessmentResponseDto response = cardiovascularRiskService.assess(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/addmetrics")
    public void addhealth_metrics(@RequestBody Health_metrics health_metrics){
        health_metricsService.addHealth_metrics(health_metrics.getUser_id(),health_metrics.getCholesterol(),
                health_metrics.getBody_temperature(),health_metrics.getSpo2());
    }
    @PostMapping("/calculate/{id}")
    public double calculate(@PathVariable("id") Integer id) {
        return 1.0;
    }
    @GetMapping("/cardiovascular/{id}")
    public ResponseEntity<Object> getcardiovascular(@PathVariable("id") Integer id) {
        User user = userservice.finduserbyid(id).get(0);
        Integer smoke = user.getSmoke();
        Integer age = user.getAge();
        String gender = user.getGender();
        Float cholesterol = health_metricsService.finduserbyid(id).get(0).getCholesterol();

        // Blood pressure / heart rate now come live from Google Fit instead
        // of the manually-entered Health Metrics record.
        Float blood_pressure;
        Float heart_rate;
        try {
            blood_pressure = (float) googleFitDataService.getBloodPressureAverage(user);
            heart_rate = (float) googleFitDataService.getHeartRateAverage(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of("error", "Could not get heart rate/blood pressure from Google Fit: " + e.getMessage()));
        }

        Double cardiovascular = health_metricsService.cardiovascular(age,blood_pressure,cholesterol,gender,heart_rate,true);
        String status = health_metricsService.estimateHeartAttackRisk(cardiovascular);
        return ResponseEntity.ok(new CardiovascularResponse(cardiovascular, status));
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
                id,health_metrics.getCholesterol(),health_metrics.getBody_temperature(),health_metrics.getSpo2());

        return ResponseEntity.ok("Health updated successfully");
    }

}