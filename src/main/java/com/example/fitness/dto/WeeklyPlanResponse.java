package com.example.fitness.dto;

import java.util.LinkedHashMap;
import java.util.List;

public class WeeklyPlanResponse {

    private LinkedHashMap<String, List<PlannedExercise>> planByDay;
    private int weeklyBudgetMinutes;
    private int totalMinutesUsed;
    private int totalValue;

    public LinkedHashMap<String, List<PlannedExercise>> getPlanByDay() { return planByDay; }
    public void setPlanByDay(LinkedHashMap<String, List<PlannedExercise>> planByDay) { this.planByDay = planByDay; }

    public int getWeeklyBudgetMinutes() { return weeklyBudgetMinutes; }
    public void setWeeklyBudgetMinutes(int weeklyBudgetMinutes) { this.weeklyBudgetMinutes = weeklyBudgetMinutes; }

    public int getTotalMinutesUsed() { return totalMinutesUsed; }
    public void setTotalMinutesUsed(int totalMinutesUsed) { this.totalMinutesUsed = totalMinutesUsed; }

    public int getTotalValue() { return totalValue; }
    public void setTotalValue(int totalValue) { this.totalValue = totalValue; }
}