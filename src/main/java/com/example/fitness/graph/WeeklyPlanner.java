package com.example.fitness.graph;

import java.util.*;

/**
 * Plain, framework-free planner that turns one or more progression "chains"
 * (one per selected muscle group, already in required easy-to-hard order)
 * into a day-by-day weekly schedule under a per-day time budget.
 *
 * This is NOT plain 0/1 knapsack. Two rules make it different, and both come
 * directly from how the feature is supposed to behave:
 *
 *  1. From any one chain, you may only ever take a PREFIX of what's left in
 *     it (you can't do Standard Push-up before Wall Push-up and Knee
 *     Push-up, whether that happens on the same day or a different day).
 *     So instead of "take item i or don't", each day's decision for a chain
 *     is "how many of the next remaining items in this chain do I take
 *     today" - a choice of prefix length, not a subset.
 *  2. Multiple chains (multiple muscle groups) are combined in the same
 *     day's budget, picking at most one prefix-length choice per chain.
 *
 * That combination - one choice per group, chosen from a small set of
 * candidate options per group, to maximize value under one shared capacity -
 * is the Multi-Choice Knapsack Problem (MCKP), a standard generalization of
 * 0/1 knapsack. Here the "choices" for a group are exactly its possible
 * prefix lengths (0, 1, 2, ... up to everything left in that chain).
 */
public class WeeklyPlanner {

    // ------------------------------------------------------------------
    // Shared types
    // ------------------------------------------------------------------

    /** One exercise placed in a chain, with its time cost and training value. */
    public record ChainItem(
            int exerciseId,
            int cost,
            int value,
            boolean substituted,
            Integer originalExerciseId
    ) {
        public ChainItem(int exerciseId, int cost, int value) {
            this(exerciseId, cost, value, false, null);
        }
    }

    /** What a single day ends up with: which items, in order, and the totals. */
    public record DayPlan(List<ChainItem> items, int totalCost, int totalValue) {
        static final DayPlan EMPTY = new DayPlan(List.of(), 0, 0);
    }

    // ------------------------------------------------------------------
    // 1) Chain construction - walk a muscle group's topological order,
    //    swap in a BFS-found substitute wherever equipment blocks a step,
    //    and drop the step entirely if no substitute is reachable either.
    //    This is where the module reuses ExerciseGraph's own BFS
    //    (findSubstitutes) and Kahn's-algorithm output (the topoOrder
    //    passed in), rather than re-solving either problem.
    // ------------------------------------------------------------------
    public static List<ChainItem> buildChain(
            List<Integer> topoOrder,
            Map<Integer, Integer> costById,
            Map<Integer, Integer> valueById,
            Set<Integer> unavailableIds,
            ExerciseGraph graph
    ) {
        List<ChainItem> chain = new ArrayList<>();
        Set<Integer> usedAsSubstitute = new HashSet<>();

        for (int exerciseId : topoOrder) {
            if (!unavailableIds.contains(exerciseId)) {
                chain.add(new ChainItem(exerciseId, costById.get(exerciseId), valueById.get(exerciseId)));
                continue;
            }
            // Blocked by equipment: look for the closest reachable substitute
            // that isn't itself blocked and hasn't already been used earlier
            // in this same chain.
            Set<Integer> excludedForThisLookup = new HashSet<>(unavailableIds);
            excludedForThisLookup.addAll(usedAsSubstitute);
            List<Integer> found = graph.findSubstitutes(exerciseId, excludedForThisLookup, 1);
            if (!found.isEmpty()) {
                int substituteId = found.get(0);
                usedAsSubstitute.add(substituteId);
                chain.add(new ChainItem(
                        substituteId, costById.get(substituteId), valueById.get(substituteId),
                        true, exerciseId));
            }
            // else: no reachable substitute - this step is dropped, chain continues.
        }
        return chain;
    }

    // ------------------------------------------------------------------
    // 2) Weekly scheduling - process days in order, running one
    //    Multi-Choice Knapsack per day over whatever each chain has left,
    //    then advancing every chain's pointer past whatever that day chose.
    //
    //    Honest limitation: this is a day-by-day greedy allocation, not a
    //    single global optimum across the whole week. Each day's own choice
    //    IS exactly optimal given what previous days already consumed, but
    //    an earlier day taking slightly less could occasionally allow a
    //    better total later in the week. Solving for the true 7-day optimum
    //    would need a much larger joint DP across all days at once; this
    //    trades a small amount of optimality for an approach that's
    //    tractable to build, test and explain.
    // ------------------------------------------------------------------
    public static LinkedHashMap<String, DayPlan> planWeek(
            Map<String, List<ChainItem>> chainsByGroup,
            LinkedHashMap<String, Integer> dailyBudgetMinutes
    ) {
        // Mutable remaining-items view per group; we consume from the front.
        Map<String, Deque<ChainItem>> remaining = new LinkedHashMap<>();
        for (var entry : chainsByGroup.entrySet()) {
            remaining.put(entry.getKey(), new ArrayDeque<>(entry.getValue()));
        }

        LinkedHashMap<String, DayPlan> result = new LinkedHashMap<>();

        for (var dayEntry : dailyBudgetMinutes.entrySet()) {
            String day = dayEntry.getKey();
            int budget = dayEntry.getValue();

            if (budget <= 0) {
                result.put(day, DayPlan.EMPTY);
                continue;
            }

            DayPlan plan = solveDay(remaining, budget);
            result.put(day, plan);

            // Advance each chain's pointer past whatever today consumed.
            for (ChainItem item : plan.items()) {
                for (Deque<ChainItem> queue : remaining.values()) {
                    if (!queue.isEmpty() && queue.peekFirst().exerciseId() == item.exerciseId()) {
                        queue.pollFirst();
                        break;
                    }
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Multi-Choice Knapsack for a single day: for every group, the
    // candidate "choices" are its possible prefix lengths (0..remaining
    // size), each with a precomputed cumulative cost/value. Pick exactly
    // one choice per group to maximize total value within `budget`.
    //
    // Time: O(numGroups * maxPrefixLen * budget)   Space: O(budget)
    // ------------------------------------------------------------------
    private static DayPlan solveDay(Map<String, Deque<ChainItem>> remaining, int budget) {
        List<String> groups = new ArrayList<>(remaining.keySet());
        // prefixCost[g][j] / prefixValue[g][j] = cumulative cost/value of
        // taking the first j remaining items of group g (j = 0 means none).
        List<int[]> prefixCosts = new ArrayList<>();
        List<int[]> prefixValues = new ArrayList<>();
        List<List<ChainItem>> remainingLists = new ArrayList<>();

        for (String g : groups) {
            List<ChainItem> items = new ArrayList<>(remaining.get(g));
            remainingLists.add(items);
            int[] cost = new int[items.size() + 1];
            int[] value = new int[items.size() + 1];
            for (int j = 1; j <= items.size(); j++) {
                cost[j] = cost[j - 1] + items.get(j - 1).cost();
                value[j] = value[j - 1] + items.get(j - 1).value();
            }
            prefixCosts.add(cost);
            prefixValues.add(value);
        }

        int n = groups.size();
        // dp[k][t] = best total value using the first k groups with total cost <= t.
        int[][] dp = new int[n + 1][budget + 1];
        // choice[k][t] = prefix length chosen for group k-1 achieving dp[k][t].
        int[][] choice = new int[n + 1][budget + 1];

        for (int k = 1; k <= n; k++) {
            int[] cost = prefixCosts.get(k - 1);
            int[] value = prefixValues.get(k - 1);
            for (int t = 0; t <= budget; t++) {
                int best = dp[k - 1][t]; // j = 0 (take nothing from this group today)
                int bestJ = 0;
                for (int j = 1; j < cost.length; j++) {
                    if (cost[j] > t) break; // costs are non-decreasing in j
                    int candidate = dp[k - 1][t - cost[j]] + value[j];
                    if (candidate > best) {
                        best = candidate;
                        bestJ = j;
                    }
                }
                dp[k][t] = best;
                choice[k][t] = bestJ;
            }
        }

        // Reconstruct: walk back from (n, budget), recovering each group's chosen prefix length.
        int[] chosenPrefixLen = new int[n];
        int t = budget;
        for (int k = n; k >= 1; k--) {
            int j = choice[k][t];
            chosenPrefixLen[k - 1] = j;
            t -= prefixCosts.get(k - 1)[j];
        }

        List<ChainItem> chosenItems = new ArrayList<>();
        int totalCost = 0, totalValue = 0;
        for (int i = 0; i < n; i++) {
            int j = chosenPrefixLen[i];
            List<ChainItem> items = remainingLists.get(i);
            for (int p = 0; p < j; p++) {
                chosenItems.add(items.get(p));
            }
            totalCost += prefixCosts.get(i)[j];
            totalValue += prefixValues.get(i)[j];
        }
        return new DayPlan(chosenItems, totalCost, totalValue);
    }
}