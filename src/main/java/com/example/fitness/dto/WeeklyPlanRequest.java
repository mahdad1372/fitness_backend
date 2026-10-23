package com.example.fitness.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;

public class WeeklyPlanRequest {

    private LinkedHashMap<String, Integer> dailyBudgetMinutes;

    private Set<String> muscleGroups;
    @Deprecated
    private Set<String> availableEquipment;

    private LocalDate weekStartDate;
    private Integer studentId;
    public LinkedHashMap<String, Integer> getDailyBudgetMinutes() {
        return dailyBudgetMinutes;
    }

    public void setDailyBudgetMinutes(LinkedHashMap<String, Integer> dailyBudgetMinutes) {
        this.dailyBudgetMinutes = dailyBudgetMinutes;
    }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Set<String> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(Set<String> muscleGroups) {
        this.muscleGroups = muscleGroups;
    }

    @Deprecated
    public Set<String> getAvailableEquipment() {
        return availableEquipment;
    }

    @Deprecated
    public void setAvailableEquipment(Set<String> availableEquipment) {
        this.availableEquipment = availableEquipment;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }
}