package com.example.fitness.graph;

/**
 * Stage 1 of the Smart Meal Planner: turns body stats, a goal and an
 * activity level into daily macro targets (calories, protein, carbs, fat).
 *
 * This is deliberately NOT the complex part of the feature - it's a
 * standard, well-established sports-nutrition formula (Mifflin-St Jeor BMR
 * -> TDEE -> goal adjustment -> gram-per-kilogram macro split), included as
 * plain arithmetic so the whole pipeline runs in one request rather than
 * needing a separate lookup step. The actual algorithmic complexity is
 * entirely in {@link MealPlanner}'s multi-dimensional knapsack, which
 * consumes this class's output as its four target ceilings.
 */
public class MacroCalculator {

    public enum Goal { MUSCLE_GAIN, FAT_LOSS, MAINTENANCE }

    public enum ActivityLevel {
        SEDENTARY(1.2), LIGHTLY_ACTIVE(1.375), MODERATELY_ACTIVE(1.55),
        ACTIVE(1.725), VERY_ACTIVE(1.9);

        public final double factor;
        ActivityLevel(double factor) { this.factor = factor; }
    }

    public record MacroTargets(int calories, int protein, int carbs, int fat) {}

    public static MacroTargets calculate(int age, String gender, float weightKg, float heightCm,
                                         Goal goal, ActivityLevel activityLevel) {
        // Mifflin-St Jeor - the formula most current sports-nutrition
        // guidance treats as the most accurate general-purpose BMR estimate
        // (more accurate than the older Harris-Benedict equation).
        double bmr = 10 * weightKg + 6.25 * heightCm - 5 * age
                + ("male".equalsIgnoreCase(gender) ? 5 : -161);
        double tdee = bmr * activityLevel.factor;

        double calorieTarget = switch (goal) {
            case MUSCLE_GAIN -> tdee * 1.12;   // ~12% surplus
            case FAT_LOSS -> tdee * 0.80;      // ~20% deficit
            case MAINTENANCE -> tdee;
        };

        // Grams per kilogram of bodyweight - standard sports-nutrition
        // ranges, picked at one representative point per goal rather than
        // a range, since the knapsack needs one concrete ceiling per
        // macro, not a band to aim within.
        double proteinPerKg = switch (goal) {
            case MUSCLE_GAIN -> 2.0;
            case FAT_LOSS -> 2.2;   // kept relatively high in a deficit to help protect muscle
            case MAINTENANCE -> 1.8;
        };
        double fatPerKg = switch (goal) {
            case MUSCLE_GAIN -> 0.9;
            case FAT_LOSS -> 0.8;
            case MAINTENANCE -> 0.9;
        };

        double proteinGrams = proteinPerKg * weightKg;
        double fatGrams = fatPerKg * weightKg;
        double proteinCalories = proteinGrams * 4;
        double fatCalories = fatGrams * 9;

        // Guard rail: protein and fat are set purely from body weight
        // (g/kg), independent of the calorie target, so for a small enough
        // calorie target (an aggressive deficit, or just a lighter person)
        // they can together exceed the whole budget on their own - leaving
        // zero or negative room for carbs. A 0g carb target isn't just an
        // unrealistic diet, it also silently disqualifies almost every food
        // with any carbohydrate content at all in stage 2's knapsack. So
        // protein and fat are scaled down together (preserving their
        // ratio) whenever they'd eat into more than MIN_CARB_FRACTION of
        // the calorie target, guaranteeing carbs always get a sane floor.
        double MIN_CARB_FRACTION = 0.15;
        double maxProteinAndFatCalories = calorieTarget * (1 - MIN_CARB_FRACTION);
        if (proteinCalories + fatCalories > maxProteinAndFatCalories && proteinCalories + fatCalories > 0) {
            double scale = maxProteinAndFatCalories / (proteinCalories + fatCalories);
            proteinGrams *= scale;
            fatGrams *= scale;
            proteinCalories = proteinGrams * 4;
            fatCalories = fatGrams * 9;
        }

        // Carbs fill whatever calories are left after protein and fat are
        // accounted for (4 kcal/g). The guard above means this is only
        // ever 0 in the degenerate case of a zero or negative calorie
        // target, not from an ordinary protein/fat combination.
        double carbCalories = Math.max(0, calorieTarget - proteinCalories - fatCalories);
        double carbGrams = carbCalories / 4;

        return new MacroTargets(
                (int) Math.round(calorieTarget),
                (int) Math.round(proteinGrams),
                (int) Math.round(carbGrams),
                (int) Math.round(fatGrams)
        );
    }
}