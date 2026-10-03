package com.example.fitness.entitties;

import jakarta.persistence.*;

@Table(name = "exercises")
@Entity
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer exercise_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String muscle_group;

    // null means bodyweight-only, no equipment needed
    private String equipment_required;

    @Column(nullable = false)
    private Integer difficulty_level; // 1 (easiest) .. 10 (hardest)
    private Integer duration_minutes;
    public Integer getExercise_id() {
        return exercise_id;
    }

    public void setExercise_id(Integer exercise_id) {
        this.exercise_id = exercise_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMuscle_group() {
        return muscle_group;
    }

    public void setMuscle_group(String muscle_group) {
        this.muscle_group = muscle_group;
    }

    public String getEquipment_required() {
        return equipment_required;
    }

    public void setEquipment_required(String equipment_required) {
        this.equipment_required = equipment_required;
    }

    public Integer getDifficulty_level() {
        return difficulty_level;
    }

    public void setDifficulty_level(Integer difficulty_level) {
        this.difficulty_level = difficulty_level;
    }
    public Integer getDuration_minutes() {
        return duration_minutes;
    }

    public void setDuration_minutes(Integer duration_minutes) {
        this.duration_minutes = duration_minutes;
    }
}