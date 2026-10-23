package com.example.fitness.entitties;

import jakarta.persistence.*;

import java.util.Date;

/**
 * One exercise inside a saved WeeklyPlan - the persisted mirror of
 * PlannedExercise, one row per item, in the exact order
 * WeeklyPlannerService produced them in (item_order), so a restored plan
 * renders identically to the one that was generated.
 */
@Table(name = "weekly_plan_items")
@Entity
public class WeeklyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer item_id;

    @Column(nullable = false)
    private Integer plan_id;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    private Integer item_order;

    @Column(nullable = false)
    private Integer exercise_id;

    @Column(nullable = false)
    private Integer duration_minutes;

    @Column(nullable = false)
    private Integer value;

    @Column(nullable = false)
    private Integer substituted = 0;

    @Column(nullable = true)
    private Integer original_exercise_id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date start_time;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date end_time;

    @Column(nullable = true)
    private String equipment_name;

    @Column(nullable = true, length = 500)
    private String scheduling_note;
    @Column(nullable = false)
    private Integer completed = 0;

    public Integer getCompleted() { return completed; }
    public void setCompleted(Integer completed) { this.completed = completed; }
    public Integer getItem_id() { return item_id; }
    public void setItem_id(Integer item_id) { this.item_id = item_id; }
    public Integer getPlan_id() { return plan_id; }
    public void setPlan_id(Integer plan_id) { this.plan_id = plan_id; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public Integer getItem_order() { return item_order; }
    public void setItem_order(Integer item_order) { this.item_order = item_order; }
    public Integer getExercise_id() { return exercise_id; }
    public void setExercise_id(Integer exercise_id) { this.exercise_id = exercise_id; }
    public Integer getDuration_minutes() { return duration_minutes; }
    public void setDuration_minutes(Integer duration_minutes) { this.duration_minutes = duration_minutes; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public Integer getSubstituted() { return substituted; }
    public void setSubstituted(Integer substituted) { this.substituted = substituted; }
    public Integer getOriginal_exercise_id() { return original_exercise_id; }
    public void setOriginal_exercise_id(Integer original_exercise_id) { this.original_exercise_id = original_exercise_id; }
    public Date getStart_time() { return start_time; }
    public void setStart_time(Date start_time) { this.start_time = start_time; }
    public Date getEnd_time() { return end_time; }
    public void setEnd_time(Date end_time) { this.end_time = end_time; }
    public String getEquipment_name() { return equipment_name; }
    public void setEquipment_name(String equipment_name) { this.equipment_name = equipment_name; }
    public String getScheduling_note() { return scheduling_note; }
    public void setScheduling_note(String scheduling_note) { this.scheduling_note = scheduling_note; }
}