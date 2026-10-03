package com.example.fitness.services;

import com.example.fitness.dto.PlannedExercise;
import com.example.fitness.dto.WeeklyPlanRequest;
import com.example.fitness.dto.WeeklyPlanResponse;
import com.example.fitness.entitties.Exercise;
import com.example.fitness.graph.ExerciseGraph;
import com.example.fitness.graph.WeeklyPlanner;
import com.example.fitness.graph.WeeklyPlanner.ChainItem;
import com.example.fitness.graph.WeeklyPlanner.DayPlan;
import com.example.fitness.repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyPlannerService {

    private final ExerciseRepository exerciseRepository;
    // Reused rather than duplicated: buildGraph() is the same @Cacheable
    // graph Find Substitute and Progression Roadmap already use, and
    // progressionOrder()/findSubstitutes() on it are the same Kahn's-
    // algorithm and BFS methods those features call.
    private final ExerciseGraphService exerciseGraphService;

    public WeeklyPlanResponse generatePlan(WeeklyPlanRequest request) {
        List<Exercise> allExercises = exerciseRepository.findAll();
        Map<Integer, Exercise> exerciseById = allExercises.stream()
                .collect(Collectors.toMap(Exercise::getExercise_id, e -> e));

        Map<Integer, Integer> costById = new HashMap<>();
        Map<Integer, Integer> valueById = new HashMap<>();
        Set<Integer> unavailableIds = new HashSet<>();

        for (Exercise ex : allExercises) {
            // Fallback estimate keeps the feature usable before every row
            // has an explicit duration_minutes filled in.
            int cost = ex.getDuration_minutes() != null
                    ? ex.getDuration_minutes()
                    : 3 + ex.getDifficulty_level();
            costById.put(ex.getExercise_id(), cost);

            // Simple, tunable heuristic: harder exercises count for more
            // training value. Swappable later for a richer scoring function
            // without touching the planning algorithm itself.
            valueById.put(ex.getExercise_id(), ex.getDifficulty_level());

            if (ex.getEquipment_required() != null
                    && !request.getAvailableEquipment().contains(ex.getEquipment_required())) {
                unavailableIds.add(ex.getExercise_id());
            }
        }

        ExerciseGraph graph = exerciseGraphService.buildGraph();

        // One independent progression chain per selected muscle group - see
        // WeeklyPlanner's class-level note on why chains stay independent
        // rather than being merged into a single global order.
        Map<String, List<ChainItem>> chainsByGroup = new LinkedHashMap<>();
        for (String muscleGroup : request.getMuscleGroups()) {
            List<Integer> groupNodeIds = allExercises.stream()
                    .filter(e -> e.getMuscle_group().equalsIgnoreCase(muscleGroup))
                    .map(Exercise::getExercise_id)
                    .collect(Collectors.toList());

            List<Integer> topoOrder = graph.progressionOrder(new LinkedHashSet<>(groupNodeIds));

            List<ChainItem> chain = WeeklyPlanner.buildChain(
                    topoOrder, costById, valueById, unavailableIds, graph);

            chainsByGroup.put(muscleGroup, chain);
        }

        LinkedHashMap<String, DayPlan> planByDay = WeeklyPlanner.planWeek(
                chainsByGroup, request.getDailyBudgetMinutes());

        return toResponse(planByDay, exerciseById, request.getDailyBudgetMinutes());
    }

    private WeeklyPlanResponse toResponse(
            LinkedHashMap<String, DayPlan> planByDay,
            Map<Integer, Exercise> exerciseById,
            LinkedHashMap<String, Integer> dailyBudgetMinutes
    ) {
        LinkedHashMap<String, List<PlannedExercise>> dto = new LinkedHashMap<>();
        int totalMinutesUsed = 0;
        int totalValue = 0;

        for (var entry : planByDay.entrySet()) {
            DayPlan plan = entry.getValue();
            List<PlannedExercise> dayDto = new ArrayList<>();
            for (ChainItem item : plan.items()) {
                Exercise ex = exerciseById.get(item.exerciseId());
                PlannedExercise p = new PlannedExercise();
                p.setExerciseId(ex.getExercise_id());
                p.setName(ex.getName());
                p.setMuscleGroup(ex.getMuscle_group());
                p.setDurationMinutes(item.cost());
                p.setValue(item.value());
                p.setSubstituted(item.substituted());
                if (item.substituted()) {
                    p.setOriginalExerciseId(item.originalExerciseId());
                    Exercise original = exerciseById.get(item.originalExerciseId());
                    p.setOriginalName(original != null ? original.getName() : null);
                }
                dayDto.add(p);
            }
            dto.put(entry.getKey(), dayDto);
            totalMinutesUsed += plan.totalCost();
            totalValue += plan.totalValue();
        }

        WeeklyPlanResponse response = new WeeklyPlanResponse();
        response.setPlanByDay(dto);
        response.setWeeklyBudgetMinutes(dailyBudgetMinutes.values().stream().mapToInt(Integer::intValue).sum());
        response.setTotalMinutesUsed(totalMinutesUsed);
        response.setTotalValue(totalValue);
        return response;
    }
}