package com.example.fitness.services;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Complexlogic {
    private final UserRepository userRepository;
    public Complexlogic(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Map<String, Object> bmi(Integer id) {
        List<User> users = userRepository.findByUser_id(id);

        if (users.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = users.get(0);
        Float weight = user.getWeight();
        Float height = user.getHeight();
        Float bmi = weight / (height * height);

        String status;
        if (bmi < 18.5) status = "Underweight";
        else if (bmi < 25) status = "Normal weight";
        else if (bmi < 30) status = "Overweight";
        else if (bmi < 35) status = "Obese (Class I)";
        else if (bmi < 40) status = "Obese (Class II)";
        else status = "Obese (Class III)";

        // Round to 1 decimal
        double roundedBmi = Math.round(bmi * 10) / 10.0;

        Map<String, Object> response = new HashMap<>();
        response.put("bmi", roundedBmi);
        response.put("status", status);

        return response;
    }

    public String performance(Float totalDuration , Integer totalWorkouts,
                              Float totalCalories,Integer workoutsThisWeek) {

        Float avgDuration = (Float) totalDuration / (float) totalWorkouts;
        Float intensityFactor = (float) (totalCalories / totalDuration);
        Float consistency = (float) workoutsThisWeek / 5;
        Float performanceScore = (avgDuration * intensityFactor * consistency);

        String Status;

        if (performanceScore < 20) {
            Status = "Poor";
        } else if (performanceScore < 40) {
            Status = "Needs Improvement";
        } else if (performanceScore < 60) {
            Status = "Good";
        } else if (performanceScore < 80) {
            Status = "Very Good";
        } else {
            Status = "Excellent";
        }
        String massage = "The performance score is " + performanceScore + " and your performance is "
                + Status;
        return massage;
    }

}
