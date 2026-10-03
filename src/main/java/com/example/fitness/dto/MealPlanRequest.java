package com.example.fitness.dto;

import java.util.Set;

public class MealPlanRequest {

    // Passed explicitly rather than read off the security principal -
    // same convention FoodsController already uses everywhere else in
    // this codebase.
    private Integer userId;

    // Optional overrides for the body stats Stage 1 needs. Null means
    // "use what's on the User record" - lets the planner run without
    // asking again for anything already on the profile, while still
    // letting the page override on a one-off basis (e.g. weight changed
    // since the profile was last updated).
    private Integer age;
    private String gender;
    private Float weightKg;
    private Float heightCm;

    // "MUSCLE_GAIN" | "FAT_LOSS" | "MAINTENANCE"
    private String goal;
    // "SEDENTARY" | "LIGHTLY_ACTIVE" | "MODERATELY_ACTIVE" | "ACTIVE" | "VERY_ACTIVE"
    private String activityLevel;

    // If ALL FOUR are provided, Stage 1's calculation is skipped entirely
    // and these are used as the knapsack's ceilings directly - this is
    // the "adjust before optimizing" override shown on the input screen.
    // Partial overrides aren't supported: it's all four or none, so the
    // targets always stay a consistent set rather than a mix of computed
    // and manually-edited numbers.
    private Integer manualCalories;
    private Integer manualProtein;
    private Integer manualCarbs;
    private Integer manualFat;

    // Food ids to leave out of today's candidates (the unchecked rows on
    // the input screen). Null/empty means "use every logged food".
    private Set<Integer> excludeFoodIds;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Float getWeightKg() { return weightKg; }
    public void setWeightKg(Float weightKg) { this.weightKg = weightKg; }

    public Float getHeightCm() { return heightCm; }
    public void setHeightCm(Float heightCm) { this.heightCm = heightCm; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Integer getManualCalories() { return manualCalories; }
    public void setManualCalories(Integer manualCalories) { this.manualCalories = manualCalories; }

    public Integer getManualProtein() { return manualProtein; }
    public void setManualProtein(Integer manualProtein) { this.manualProtein = manualProtein; }

    public Integer getManualCarbs() { return manualCarbs; }
    public void setManualCarbs(Integer manualCarbs) { this.manualCarbs = manualCarbs; }

    public Integer getManualFat() { return manualFat; }
    public void setManualFat(Integer manualFat) { this.manualFat = manualFat; }

    public Set<Integer> getExcludeFoodIds() { return excludeFoodIds; }
    public void setExcludeFoodIds(Set<Integer> excludeFoodIds) { this.excludeFoodIds = excludeFoodIds; }
}