package com.example.fitness.dto;

import java.util.LinkedHashMap;
import java.util.Set;

public class WeeklyPlanRequest {

    // Keys are day names ("MONDAY".."SUNDAY"), values are minutes available
    // that day (0 = rest day). A LinkedHashMap here matters: Jackson
    // deserializes a JSON object into a LinkedHashMap by default, which
    // preserves the key order exactly as the frontend sent it - so as long
    // as the frontend sends Monday first through Sunday last, the planner
    // processes the week in the right order without needing a separate
    // day-of-week sort here.
    private LinkedHashMap<String, Integer> dailyBudgetMinutes;

    private Set<String> muscleGroups;

    // Equipment the user HAS available (opposite framing from Find
    // Substitute's "unavailableEquipment" - here it's simpler to say what
    // you've got, since the planner is drawing from many muscle groups at
    // once rather than excluding equipment for one specific exercise).
    private Set<String> availableEquipment;

    public LinkedHashMap<String, Integer> getDailyBudgetMinutes() {
        return dailyBudgetMinutes;
    }

    public void setDailyBudgetMinutes(LinkedHashMap<String, Integer> dailyBudgetMinutes) {
        this.dailyBudgetMinutes = dailyBudgetMinutes;
    }

    public Set<String> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(Set<String> muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    public Set<String> getAvailableEquipment() {
        return availableEquipment;
    }

    public void setAvailableEquipment(Set<String> availableEquipment) {
        this.availableEquipment = availableEquipment;
    }
}