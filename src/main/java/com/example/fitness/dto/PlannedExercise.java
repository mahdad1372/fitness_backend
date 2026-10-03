package com.example.fitness.dto;

public class PlannedExercise {

    private Integer exerciseId;
    private String name;
    private String muscleGroup;
    private Integer durationMinutes;
    private Integer value;

    // True when this slot wasn't the exercise the progression chain actually
    // called for - it was blocked by missing equipment and swapped in via
    // the same BFS substitute search Find Substitute uses.
    private boolean substituted;
    private Integer originalExerciseId;
    private String originalName;

    public Integer getExerciseId() { return exerciseId; }
    public void setExerciseId(Integer exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public boolean isSubstituted() { return substituted; }
    public void setSubstituted(boolean substituted) { this.substituted = substituted; }

    public Integer getOriginalExerciseId() { return originalExerciseId; }
    public void setOriginalExerciseId(Integer originalExerciseId) { this.originalExerciseId = originalExerciseId; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
}