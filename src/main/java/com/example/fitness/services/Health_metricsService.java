package com.example.fitness.services;
import com.example.fitness.dto.CardiovascularDTO;
import com.example.fitness.dto.CardiovascularResponse;
import com.example.fitness.entitties.Goals;
import com.example.fitness.entitties.Health_metrics;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.Health_metricsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class Health_metricsService {
    private Double cardio = 0.0;
    private final Health_metricsRepository health_metricsRepository;
    public Health_metricsService(Health_metricsRepository health_metricsRepository) {
        this.health_metricsRepository = health_metricsRepository;
    }
    public void addHealth_metrics(Integer user_id, Float cholesterol, Float blood_pressure, Float heart_rate) {

        health_metricsRepository.addHealth_metrics(user_id, cholesterol, blood_pressure, heart_rate);

    }
    public void deleteHealthById(Integer id){
        health_metricsRepository.deleteHealth_metricsByhealth_id(id);
    }
    public void updateHealth_metrics(Integer healthmetric_id, Float cholesterol, Float blood_pressure, Float heart_rate)
    {
        health_metricsRepository.Healthmetric_update(healthmetric_id, cholesterol, blood_pressure, heart_rate
        );
    }
    public List<Health_metrics> finduserbyid(Integer id) {
        List<Health_metrics> health_metrics = new ArrayList<>();
        health_metricsRepository.findByUser_id(id).forEach(health_metrics::add);
        return health_metrics;
    }
    public List<Health_metrics> getGoalsById(Integer id){
        List<Health_metrics> health_metrics = new ArrayList<>();
        health_metricsRepository.findBy_id(id).forEach(health_metrics::add);
        return health_metrics;

    }

    public double  cardiovascular(Integer age, Float bloodPressure, Float cholesterol,
                                  String gender, Float heartRate, Boolean smoker) {


        Integer smk = 0;
        if (smoker == true){
            smk =1;
        }else {
            smk = 0;
        }
        // Estimate HDL from total cholesterol (you can change this assumption)
        double estimatedHdl = cholesterol * 0.20;

        double cardiovascular;

        // --------------------------
        // MALE FORMULA
        // --------------------------
        if (gender.equalsIgnoreCase("male")) {
            cardiovascular =
                    0.04826 * age +
                            1.600 * cholesterol -
                            0.523 * estimatedHdl +
                            1.148 * bloodPressure +
                            0.428 * smk;

        } else {
            // --------------------------
            // FEMALE FORMULA
            // --------------------------
            cardiovascular =
                    0.33766 * age +
                            0.26138 * cholesterol -
                            0.7181 * estimatedHdl +
                            2.81291 * bloodPressure +
                            0.52873 * smk;
        }

        // Heart rate factor (custom)
        cardiovascular += 0.015 * (heartRate - 70);
        // Final transformation
        return cardiovascular;
    }
    public String estimateHeartAttackRisk(double cardiovascularScore) {

        if (cardiovascularScore < 200) {
            return "Low risk";
        } else if (cardiovascularScore < 400) {
            return "Moderate risk";
        } else if (cardiovascularScore < 700) {
            return "High risk";
        } else {
            return "Very high risk";
        }
    }
    public CardiovascularResponse cardiovascular(Integer id) {
        List<CardiovascularDTO> cardiovascular = health_metricsRepository.cardiovascular(id);

        if (!cardiovascular.isEmpty()) {
            CardiovascularDTO dto = cardiovascular.get(0);  // get first item
            Double cardio = cardiovascular(dto.getAge(), dto.getBloodPressure(),
                    dto.getCholesterol(), dto.getGender(),
                    dto.getHeartRate(), dto.getSmoke());

            String status = estimateHeartAttackRisk(cardio); // example logic
            return new CardiovascularResponse(cardio, status);

        } else {
            return new CardiovascularResponse(0.0, "no data");
        }
    }
    public List<Health_metrics> fetchAll(){
        return health_metricsRepository.getallHealthMetrics();
    }
}
//Integer smoke = userservice.finduserbyid(id).get(0).getSmoke();
//Integer age = userservice.finduserbyid(id).get(0).getAge();
//String gender = userservice.finduserbyid(id).get(0).getGender();
//Float cholesterol = health_metricsService.finduserbyid(id).get(0).getCholesterol();
//Float blood_pressure = health_metricsService.finduserbyid(id).get(0).getBlood_pressure();
//Float heart_rate = health_metricsService.finduserbyid(id).get(0).getHeart_rate();
//Double cardiovascular = health_metricsService.cardiovascular(age,blood_pressure,cholesterol,gender,heart_rate,true);
//        return cardiovascular;