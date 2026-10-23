package com.example.fitness.services;

import com.example.fitness.dto.PlannedExercise;
import com.example.fitness.dto.WeeklyPlanRequest;
import com.example.fitness.dto.WeeklyPlanResponse;
import com.example.fitness.entitties.Equipment;
import com.example.fitness.entitties.Exercise;
import com.example.fitness.graph.ExerciseGraph;
import com.example.fitness.graph.WeeklyPlanner;
import com.example.fitness.graph.WeeklyPlanner.ChainItem;
import com.example.fitness.graph.WeeklyPlanner.DayPlan;
import com.example.fitness.repositories.EquipmentRepository;
import com.example.fitness.repositories.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyPlannerService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseGraphService exerciseGraphService;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentAvailabilityService equipmentAvailabilityService;
    private final WeeklyPlanPersistenceService weeklyPlanPersistenceService;

    public WeeklyPlanResponse generatePlan(WeeklyPlanRequest request) {
        if (request.getWeekStartDate() == null) {
            throw new RuntimeException("weekStartDate is required - it's what turns day names like \"MONDAY\" into a real calendar date to check equipment availability against.");
        }
        if (request.getStudentId() == null) {
            throw new RuntimeException("studentId is required - it's who the saved plan belongs to.");
        }

        List<Exercise> allExercises = exerciseRepository.findAll();
        Map<Integer, Exercise> exerciseById = allExercises.stream()
                .collect(Collectors.toMap(Exercise::getExercise_id, e -> e));

        Map<String, Equipment> equipmentByName = new HashMap<>();
        for (Equipment eq : equipmentRepository.findAll()) {
            equipmentByName.put(normalize(eq.getName()), eq);
        }

        Map<Integer, Integer> costById = new HashMap<>();
        Map<Integer, Integer> valueById = new HashMap<>();
        Set<Integer> unavailableIds = new HashSet<>();

        for (Exercise ex : allExercises) {
            int cost = ex.getDuration_minutes() != null
                    ? ex.getDuration_minutes()
                    : 3 + ex.getDifficulty_level();
            costById.put(ex.getExercise_id(), cost);
            valueById.put(ex.getExercise_id(), ex.getDifficulty_level());

            if (ex.getEquipment_required() != null
                    && !equipmentByName.containsKey(normalize(ex.getEquipment_required()))) {
                unavailableIds.add(ex.getExercise_id());
            }
        }

        ExerciseGraph graph = exerciseGraphService.buildGraph();

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

        LinkedHashMap<String, List<ScheduledItem>> scheduledByDay = assignTimes(
                planByDay, request.getWeekStartDate(), exerciseById, equipmentByName);

        WeeklyPlanResponse response = toResponse(scheduledByDay, exerciseById, request.getDailyBudgetMinutes());
        weeklyPlanPersistenceService.save(request.getStudentId(), request.getWeekStartDate(), response);

        return response;
    }
    public void setItemCompleted(Integer studentId, Integer itemId, boolean completed) {
        weeklyPlanPersistenceService.setItemCompleted(studentId, itemId, completed);
    }
    public Optional<WeeklyPlanResponse> findCurrentPlan(Integer studentId) {
        return weeklyPlanPersistenceService.findLatest(studentId);
    }

    private LinkedHashMap<String, List<ScheduledItem>> assignTimes(
            LinkedHashMap<String, DayPlan> planByDay,
            LocalDate weekStartDate,
            Map<Integer, Exercise> exerciseById,
            Map<String, Equipment> equipmentByName
    ) {
        LinkedHashMap<String, List<ScheduledItem>> result = new LinkedHashMap<>();
        int dayIndex = 0;
        for (var entry : planByDay.entrySet()) {
            LocalDate date = weekStartDate.plusDays(dayIndex++);
            LocalDateTime dayCloses = date.atTime(EquipmentAvailabilityService.GYM_CLOSE_HOUR, 0);
            LocalDateTime cursor = date.atTime(EquipmentAvailabilityService.GYM_OPEN_HOUR, 0);

            List<ScheduledItem> scheduled = new ArrayList<>();
            for (ChainItem item : entry.getValue().items()) {
                Exercise ex = exerciseById.get(item.exerciseId());
                Equipment equipment = ex.getEquipment_required() != null
                        ? equipmentByName.get(normalize(ex.getEquipment_required()))
                        : null;

                if (equipment == null) {
                    LocalDateTime start = cursor;
                    LocalDateTime end = start.plusMinutes(item.cost());
                    if (end.isAfter(dayCloses)) {
                        scheduled.add(new ScheduledItem(item, null, null, null,
                                "No time left before closing today - try moving this to another day."));
                        continue;
                    }
                    scheduled.add(new ScheduledItem(item, start, end, null, null));
                    cursor = end;
                } else {
                    Optional<LocalDateTime> slot = equipmentAvailabilityService.earliestSlot(
                            equipment.getEquipment_id(), date, cursor, item.cost());
                    if (slot.isEmpty()) {
                        scheduled.add(new ScheduledItem(item, null, null, equipment.getName(),
                                equipment.getName() + " is fully booked for the rest of today - try another day."));
                        continue;
                    }
                    LocalDateTime start = slot.get();
                    LocalDateTime end = start.plusMinutes(item.cost());
                    scheduled.add(new ScheduledItem(item, start, end, equipment.getName(), null));
                    cursor = end;
                }
            }
            result.put(entry.getKey(), scheduled);
        }
        return result;
    }

    private record ScheduledItem(
            ChainItem item,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String equipmentName,
            String schedulingNote
    ) {}

    private String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    private Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private WeeklyPlanResponse toResponse(
            LinkedHashMap<String, List<ScheduledItem>> scheduledByDay,
            Map<Integer, Exercise> exerciseById,
            LinkedHashMap<String, Integer> dailyBudgetMinutes
    ) {
        LinkedHashMap<String, List<PlannedExercise>> dto = new LinkedHashMap<>();
        int totalMinutesUsed = 0;
        int totalValue = 0;

        for (var entry : scheduledByDay.entrySet()) {
            List<PlannedExercise> dayDto = new ArrayList<>();
            for (ScheduledItem scheduled : entry.getValue()) {
                ChainItem item = scheduled.item();
                Exercise ex = exerciseById.get(item.exerciseId());
                PlannedExercise p = new PlannedExercise();
                p.setExerciseId(ex.getExercise_id());
                p.setName(ex.getName());
                p.setMuscleGroup(ex.getMuscle_group());
                p.setSets(ex.getSets());
                p.setRpe(ex.getRpe());
                p.setDurationMinutes(item.cost());
                p.setValue(item.value());
                p.setSubstituted(item.substituted());
                if (item.substituted()) {
                    p.setOriginalExerciseId(item.originalExerciseId());
                    Exercise original = exerciseById.get(item.originalExerciseId());
                    p.setOriginalName(original != null ? original.getName() : null);
                }
                p.setStartTime(toDate(scheduled.startTime()));
                p.setEndTime(toDate(scheduled.endTime()));
                p.setEquipmentName(scheduled.equipmentName());
                p.setSchedulingNote(scheduled.schedulingNote());
                dayDto.add(p);

                totalMinutesUsed += item.cost();
                totalValue += item.value();
            }
            dto.put(entry.getKey(), dayDto);
        }

        WeeklyPlanResponse response = new WeeklyPlanResponse();
        response.setPlanByDay(dto);
        response.setWeeklyBudgetMinutes(dailyBudgetMinutes.values().stream().mapToInt(Integer::intValue).sum());
        response.setTotalMinutesUsed(totalMinutesUsed);
        response.setTotalValue(totalValue);
        return response;
    }
}