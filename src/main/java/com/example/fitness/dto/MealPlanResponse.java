package com.example.fitness.dto;

import java.util.List;

public class MealPlanResponse {

    // What Stage 1 (MacroCalculator) computed, or the manual override if
    // one was supplied - these are the knapsack's four ceilings.
    private Integer targetCalories;
    private Integer targetProtein;
    private Integer targetCarbs;
    private Integer targetFat;

    // What the selected foods actually add up to.
    private Integer achievedCalories;
    private Integer achievedProtein;
    private Integer achievedCarbs;
    private Integer achievedFat;

    private List<PlannedFood> selectedFoods;
    // Candidates that existed but weren't chosen - not because of one
    // simple reason each (this is a combinatorial selection, not a
    // rule check), so no per-item explanation is generated; the UI can
    // still show them as "not included" the way the mockup did.
    private List<PlannedFood> excludedFoods;

    public Integer getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Integer targetCalories) { this.targetCalories = targetCalories; }

    public Integer getTargetProtein() { return targetProtein; }
    public void setTargetProtein(Integer targetProtein) { this.targetProtein = targetProtein; }

    public Integer getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(Integer targetCarbs) { this.targetCarbs = targetCarbs; }

    public Integer getTargetFat() { return targetFat; }
    public void setTargetFat(Integer targetFat) { this.targetFat = targetFat; }

    public Integer getAchievedCalories() { return achievedCalories; }
    public void setAchievedCalories(Integer achievedCalories) { this.achievedCalories = achievedCalories; }

    public Integer getAchievedProtein() { return achievedProtein; }
    public void setAchievedProtein(Integer achievedProtein) { this.achievedProtein = achievedProtein; }

    public Integer getAchievedCarbs() { return achievedCarbs; }
    public void setAchievedCarbs(Integer achievedCarbs) { this.achievedCarbs = achievedCarbs; }

    public Integer getAchievedFat() { return achievedFat; }
    public void setAchievedFat(Integer achievedFat) { this.achievedFat = achievedFat; }

    public List<PlannedFood> getSelectedFoods() { return selectedFoods; }
    public void setSelectedFoods(List<PlannedFood> selectedFoods) { this.selectedFoods = selectedFoods; }

    public List<PlannedFood> getExcludedFoods() { return excludedFoods; }
    public void setExcludedFoods(List<PlannedFood> excludedFoods) { this.excludedFoods = excludedFoods; }
}