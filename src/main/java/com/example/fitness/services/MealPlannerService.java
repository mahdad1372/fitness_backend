package com.example.fitness.services;

import com.example.fitness.dto.MealPlanRequest;
import com.example.fitness.dto.MealPlanResponse;
import com.example.fitness.dto.PlannedFood;
import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.User;
import com.example.fitness.graph.MacroCalculator;
import com.example.fitness.graph.MealPlanner;
import com.example.fitness.repositories.FoodsRepository;
import com.example.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlannerService {

    private final FoodsRepository foodsRepository;
    private final UserRepository userRepository;

    public MealPlanResponse generatePlan(MealPlanRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        MacroCalculator.MacroTargets targets = resolveTargets(request, user);

        List<Foods> foods = foodsRepository.findByUser_id(request.getUserId());
        Set<Integer> excluded = request.getExcludeFoodIds() != null
                ? request.getExcludeFoodIds() : Set.of();

        Map<Integer, Foods> foodsById = new LinkedHashMap<>();
        List<MealPlanner.FoodItem> candidates = new ArrayList<>();
        for (Foods f : foods) {
            if (excluded.contains(f.getFood_id())) continue;
            foodsById.put(f.getFood_id(), f);
            candidates.add(new MealPlanner.FoodItem(
                    f.getFood_id(),
                    f.getFood_name(),
                    f.getCategory(),
                    // Foods stores macros as Float (fractional grams from a
                    // nutrition label); the knapsack works in whole units,
                    // so each is rounded to the nearest gram/kcal here -
                    // sub-gram precision doesn't matter for meal selection.
                    Math.round(f.getCalories()),
                    Math.round(f.getProtein()),
                    Math.round(f.getCarbohydrates()),
                    Math.round(f.getFats())
            ));
        }

        MealPlanner.PlanResult result = MealPlanner.solve(candidates,
                new MealPlanner.MacroTargets(targets.calories(), targets.protein(), targets.carbs(), targets.fat()));

        Set<Integer> selectedIds = result.selected().stream()
                .map(MealPlanner.FoodItem::id).collect(Collectors.toSet());

        MealPlanResponse response = new MealPlanResponse();
        response.setTargetCalories(targets.calories());
        response.setTargetProtein(targets.protein());
        response.setTargetCarbs(targets.carbs());
        response.setTargetFat(targets.fat());
        response.setAchievedCalories(result.totalCalories());
        response.setAchievedProtein(result.totalProtein());
        response.setAchievedCarbs(result.totalCarbs());
        response.setAchievedFat(result.totalFat());
        response.setSelectedFoods(result.selected().stream().map(this::toDto).collect(Collectors.toList()));
        response.setExcludedFoods(candidates.stream()
                .filter(f -> !selectedIds.contains(f.id()))
                .map(this::toDto)
                .collect(Collectors.toList()));
        return response;
    }

    private MacroCalculator.MacroTargets resolveTargets(MealPlanRequest request, User user) {
        if (request.getManualCalories() != null && request.getManualProtein() != null
                && request.getManualCarbs() != null && request.getManualFat() != null) {
            return new MacroCalculator.MacroTargets(
                    request.getManualCalories(), request.getManualProtein(),
                    request.getManualCarbs(), request.getManualFat());
        }

        int age = request.getAge() != null ? request.getAge() : user.getAge();
        String gender = request.getGender() != null ? request.getGender() : user.getGender();
        float weightKg = request.getWeightKg() != null ? request.getWeightKg() : user.getWeight();
        float heightCm = request.getHeightCm() != null ? request.getHeightCm() : user.getHeight();

        MacroCalculator.Goal goal = MacroCalculator.Goal.valueOf(request.getGoal().toUpperCase());
        MacroCalculator.ActivityLevel activityLevel =
                MacroCalculator.ActivityLevel.valueOf(request.getActivityLevel().toUpperCase());

        return MacroCalculator.calculate(age, gender, weightKg, heightCm, goal, activityLevel);
    }

    private PlannedFood toDto(MealPlanner.FoodItem item) {
        PlannedFood dto = new PlannedFood();
        dto.setFoodId(item.id());
        dto.setName(item.name());
        dto.setMealSlot(item.mealSlot());
        dto.setCalories(item.calories());
        dto.setProtein(item.protein());
        dto.setCarbs(item.carbs());
        dto.setFat(item.fat());
        return dto;
    }
}