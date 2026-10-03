package com.example.fitness.dto;

public class PlannedFood {

    private Integer foodId;
    private String name;
    private String mealSlot;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fat;

    public Integer getFoodId() { return foodId; }
    public void setFoodId(Integer foodId) { this.foodId = foodId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMealSlot() { return mealSlot; }
    public void setMealSlot(String mealSlot) { this.mealSlot = mealSlot; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public Integer getProtein() { return protein; }
    public void setProtein(Integer protein) { this.protein = protein; }

    public Integer getCarbs() { return carbs; }
    public void setCarbs(Integer carbs) { this.carbs = carbs; }

    public Integer getFat() { return fat; }
    public void setFat(Integer fat) { this.fat = fat; }
}