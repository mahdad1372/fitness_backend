package com.example.fitness.graph;

import java.util.*;

/**
 * Stage 2 of the Smart Meal Planner: a multi-dimensional 0/1 knapsack that
 * picks which candidate foods to eat today.
 *
 * Unlike WeeklyPlanner's single-budget (minutes) knapsack, a day's meal
 * plan has to respect FOUR simultaneous nutrition ceilings at once -
 * calories, protein, carbohydrates and fat - none of which may be
 * exceeded. Within those four ceilings, total protein is maximized:
 * protein is the macro most directly tied to every goal this planner
 * supports (muscle gain, fat loss, maintenance), while the other three are
 * budgets to stay under rather than values to chase. That's a deliberate
 * simplification - not "get as close as possible to all four targets" - and
 * it's why a plan can come in under target on, say, fat: nothing left in
 * the catalog could add fat without also pushing calories or carbs over
 * their own ceilings.
 *
 * A 4-dimensional DP table's size is the PRODUCT of each dimension's
 * bucket count, not the sum, so each macro is discretized into buckets
 * before solving rather than tracked gram-for-gram - see the bucket
 * constants below. Targets are rounded DOWN to the nearest bucket and each
 * item's cost is rounded UP; that specific combination guarantees the
 * real, un-rounded total can never exceed the real target (if every
 * item's bucketed cost is >= its real cost, and the bucketed sum fits
 * within floor(target/bucket) buckets, the real sum fits within
 * floor(target/bucket)*bucket <= target). Rounding the other way, or both
 * directions the same way, would risk silently overshooting the real
 * ceiling - this is the same kind of rounding trade-off used in
 * pseudo-polynomial approximation schemes for knapsack generally.
 *
 * Verified in verify_meal_planner/ (not part of this module) against an
 * exhaustive brute-force oracle across 500+ randomized scenarios plus
 * named edge cases, since a 4-dimensional DP isn't hand-traceable the way
 * WeeklyPlanner's single-dimension knapsack or the Coach Scheduler's
 * interval scheduling were.
 */
public class MealPlanner {

    public record FoodItem(int id, String name, String mealSlot,
                           int calories, int protein, int carbs, int fat) {}

    public record MacroTargets(int calories, int protein, int carbs, int fat) {}

    public record PlanResult(List<FoodItem> selected, int totalCalories,
                             int totalProtein, int totalCarbs, int totalFat) {}

    public static final int CALORIE_BUCKET = 50;
    public static final int PROTEIN_BUCKET = 5;
    public static final int CARB_BUCKET = 10;
    public static final int FAT_BUCKET = 5;

    public static PlanResult solve(List<FoodItem> candidates, MacroTargets targets) {
        return solve(candidates, targets, CALORIE_BUCKET, PROTEIN_BUCKET, CARB_BUCKET, FAT_BUCKET);
    }

    // Bucket sizes are parameters, not just constants, so the verification
    // harness can re-run this exact algorithm with bucket = 1 (no
    // discretization at all) and cross-check it against a brute-force
    // reference operating on the real, un-bucketed numbers.
    public static PlanResult solve(List<FoodItem> candidates, MacroTargets targets,
                                   int calorieBucket, int proteinBucket, int carbBucket, int fatBucket) {
        int n = candidates.size();
        int capCal = targets.calories() / calorieBucket;   // floor - see class doc
        int capPro = targets.protein() / proteinBucket;
        int capCarb = targets.carbs() / carbBucket;
        int capFat = targets.fat() / fatBucket;

        // dp[cal][pro][carb][fat] = best total protein value achievable
        // with bucketed cost at most (cal,pro,carb,fat), using items
        // considered so far. Monotonic non-decreasing in every dimension
        // by construction (the standard 0/1 knapsack invariant, extended
        // to four dimensions), so the single corner
        // dp[capCal][capPro][capCarb][capFat] already holds the global
        // optimum for the full budget - no scan over interior cells needed.
        int[][][][] dp = new int[capCal + 1][capPro + 1][capCarb + 1][capFat + 1];
        boolean[][][][][] taken = new boolean[n][capCal + 1][capPro + 1][capCarb + 1][capFat + 1];

        for (int i = 0; i < n; i++) {
            FoodItem item = candidates.get(i);
            int wCal = ceilDiv(item.calories(), calorieBucket);
            int wPro = ceilDiv(item.protein(), proteinBucket);
            int wCarb = ceilDiv(item.carbs(), carbBucket);
            int wFat = ceilDiv(item.fat(), fatBucket);
            int value = item.protein();

            if (wCal > capCal || wPro > capPro || wCarb > capCarb || wFat > capFat) {
                continue; // can never fit even alone - nothing to update
            }

            // Backward on every dimension - standard 0/1 knapsack in-place
            // update, so each item is considered at most once.
            for (int cal = capCal; cal >= wCal; cal--) {
                for (int pro = capPro; pro >= wPro; pro--) {
                    for (int carb = capCarb; carb >= wCarb; carb--) {
                        for (int fat = capFat; fat >= wFat; fat--) {
                            int withItem = dp[cal - wCal][pro - wPro][carb - wCarb][fat - wFat] + value;
                            if (withItem > dp[cal][pro][carb][fat]) {
                                dp[cal][pro][carb][fat] = withItem;
                                taken[i][cal][pro][carb][fat] = true;
                            }
                        }
                    }
                }
            }
        }

        List<FoodItem> selected = new ArrayList<>();
        int cal = capCal, pro = capPro, carb = capCarb, fat = capFat;
        for (int i = n - 1; i >= 0; i--) {
            if (taken[i][cal][pro][carb][fat]) {
                FoodItem item = candidates.get(i);
                selected.add(item);
                cal -= ceilDiv(item.calories(), calorieBucket);
                pro -= ceilDiv(item.protein(), proteinBucket);
                carb -= ceilDiv(item.carbs(), carbBucket);
                fat -= ceilDiv(item.fat(), fatBucket);
            }
        }
        Collections.reverse(selected);

        int totalCalories = selected.stream().mapToInt(FoodItem::calories).sum();
        int totalProtein = selected.stream().mapToInt(FoodItem::protein).sum();
        int totalCarbs = selected.stream().mapToInt(FoodItem::carbs).sum();
        int totalFat = selected.stream().mapToInt(FoodItem::fat).sum();
        return new PlanResult(selected, totalCalories, totalProtein, totalCarbs, totalFat);
    }

    private static int ceilDiv(int value, int bucket) {
        return (value + bucket - 1) / bucket;
    }
}