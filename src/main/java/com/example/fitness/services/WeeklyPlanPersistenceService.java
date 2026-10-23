package com.example.fitness.services;

import com.example.fitness.dto.PlannedExercise;
import com.example.fitness.dto.WeeklyPlanResponse;
import com.example.fitness.entitties.Exercise;
import com.example.fitness.entitties.WeeklyPlan;
import com.example.fitness.entitties.WeeklyPlanItem;
import com.example.fitness.repositories.ExerciseRepository;
import com.example.fitness.repositories.WeeklyPlanItemRepository;
import com.example.fitness.repositories.WeeklyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeeklyPlanPersistenceService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final WeeklyPlanItemRepository weeklyPlanItemRepository;
    private final ExerciseRepository exerciseRepository;

    // Saves a freshly generated plan for this student/week. If a plan already
    // exists for the SAME week (student_id + week_start_date), it's deleted
    // first (items then the plan row) so regenerating a week never leaves
    // orphaned old rows behind.
    @Transactional
    public void save(Integer studentId, LocalDate weekStartDate, WeeklyPlanResponse response) {
        Date weekStartAsDate = toDate(weekStartDate);

        List<WeeklyPlan> existing = weeklyPlanRepository.findByStudent_idAndWeek_start_date(studentId, weekStartAsDate);
        for (WeeklyPlan old : existing) {
            weeklyPlanItemRepository.deleteByPlan_id(old.getPlan_id());
        }
        weeklyPlanRepository.deleteByStudent_idAndWeek_start_date(studentId, weekStartAsDate);

        WeeklyPlan plan = new WeeklyPlan();
        plan.setStudent_id(studentId);
        plan.setWeek_start_date(weekStartAsDate);
        plan.setWeekly_budget_minutes(response.getWeeklyBudgetMinutes());
        plan.setTotal_minutes_used(response.getTotalMinutesUsed());
        plan.setTotal_value(response.getTotalValue());
        plan = weeklyPlanRepository.save(plan);

        int order = 0;
        for (Map.Entry<String, List<PlannedExercise>> entry : response.getPlanByDay().entrySet()) {
            String day = entry.getKey();
            for (PlannedExercise p : entry.getValue()) {
                WeeklyPlanItem item = new WeeklyPlanItem();
                item.setPlan_id(plan.getPlan_id());
                item.setDay(day);
                item.setItem_order(order++);
                item.setExercise_id(p.getExerciseId());
                item.setDuration_minutes(p.getDurationMinutes());
                item.setValue(p.getValue());
                item.setSubstituted(p.isSubstituted() ? 1 : 0);
                item.setOriginal_exercise_id(p.getOriginalExerciseId());
                item.setStart_time(p.getStartTime());
                item.setEnd_time(p.getEndTime());
                item.setEquipment_name(p.getEquipmentName());
                item.setScheduling_note(p.getSchedulingNote());
                item = weeklyPlanItemRepository.save(item);

                // Mutate the SAME PlannedExercise object the caller's response
                // holds, so the itemId is already populated in the response
                // WeeklyPlannerController returns - no second round trip needed.
                p.setItemId(item.getItem_id());
            }
        }
    }

    // Restores whichever plan was generated most recently for this student -
    // deliberately NOT scoped to "this calendar week's Monday", because the
    // frontend's date picker defaults to the upcoming Monday, which is
    // usually a different ISO week than "today's" week. Scoping this lookup
    // by week caused the plan to silently vanish on page reload.
    public Optional<WeeklyPlanResponse> findLatest(Integer studentId) {
        List<WeeklyPlan> plans = weeklyPlanRepository.findLatestByStudent_id(studentId);
        if (plans.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toResponse(plans.get(0)));
    }

    private WeeklyPlanResponse toResponse(WeeklyPlan plan) {
        List<WeeklyPlanItem> items = weeklyPlanItemRepository.findByPlan_idOrderByItem_orderAsc(plan.getPlan_id());

        Map<Integer, Exercise> exerciseById = new HashMap<>();
        for (WeeklyPlanItem item : items) {
            loadExerciseIfMissing(exerciseById, item.getExercise_id());
            if (item.getOriginal_exercise_id() != null) {
                loadExerciseIfMissing(exerciseById, item.getOriginal_exercise_id());
            }
        }

        LinkedHashMap<String, List<PlannedExercise>> planByDay = new LinkedHashMap<>();
        for (WeeklyPlanItem item : items) {
            Exercise ex = exerciseById.get(item.getExercise_id());
            PlannedExercise p = new PlannedExercise();
            p.setExerciseId(item.getExercise_id());
            p.setName(ex != null ? ex.getName() : "(deleted exercise)");
            p.setMuscleGroup(ex != null ? ex.getMuscle_group() : null);
            p.setSets(ex != null ? ex.getSets() : null);
            p.setRpe(ex != null ? ex.getRpe() : null);
            p.setDurationMinutes(item.getDuration_minutes());
            p.setValue(item.getValue());
            p.setSubstituted(item.getSubstituted() != null && item.getSubstituted() == 1);
            p.setOriginalExerciseId(item.getOriginal_exercise_id());
            if (item.getOriginal_exercise_id() != null) {
                Exercise original = exerciseById.get(item.getOriginal_exercise_id());
                p.setOriginalName(original != null ? original.getName() : null);
            }
            p.setStartTime(item.getStart_time());
            p.setEndTime(item.getEnd_time());
            p.setEquipmentName(item.getEquipment_name());
            p.setSchedulingNote(item.getScheduling_note());
            p.setItemId(item.getItem_id());
            p.setCompleted(item.getCompleted() != null && item.getCompleted() == 1);
            planByDay.computeIfAbsent(item.getDay(), k -> new ArrayList<>()).add(p);
        }

        WeeklyPlanResponse response = new WeeklyPlanResponse();
        response.setPlanByDay(planByDay);
        response.setWeeklyBudgetMinutes(plan.getWeekly_budget_minutes());
        response.setTotalMinutesUsed(plan.getTotal_minutes_used());
        response.setTotalValue(plan.getTotal_value());
        return response;
    }

    // Flips one exercise's done/not-done checkbox. Loads the item, then the
    // plan it belongs to, and refuses the update if the plan's student_id
    // doesn't match the studentId passed in - stops one student from
    // toggling another student's checkbox just by guessing an itemId.
    @Transactional
    public void setItemCompleted(Integer studentId, Integer itemId, boolean completed) {
        WeeklyPlanItem item = weeklyPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("No such weekly plan item."));
        WeeklyPlan plan = weeklyPlanRepository.findById(item.getPlan_id())
                .orElseThrow(() -> new RuntimeException("No such weekly plan."));
        if (!plan.getStudent_id().equals(studentId)) {
            throw new RuntimeException("This weekly plan item doesn't belong to this student.");
        }
        weeklyPlanItemRepository.updateCompleted(itemId, completed ? 1 : 0);
    }

    private void loadExerciseIfMissing(Map<Integer, Exercise> cache, Integer exerciseId) {
        if (!cache.containsKey(exerciseId)) {
            exerciseRepository.findById(exerciseId).ifPresent(e -> cache.put(exerciseId, e));
        }
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}