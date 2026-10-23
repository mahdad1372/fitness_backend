package com.example.fitness.dto;

import java.util.Date;

public class PlannedExercise {

    private Integer exerciseId;
    private String name;
    private String muscleGroup;
    private Integer durationMinutes;
    private Integer value;
    private boolean substituted;
    private Integer originalExerciseId;
    private String originalName;
    private Date startTime;
    private Date endTime;
    private String equipmentName;
    private String schedulingNote;
    private Integer itemId;
    private boolean completed;
    private Integer sets;
    private Integer rpe;

    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }

    public Integer getRpe() { return rpe; }
    public void setRpe(Integer rpe) { this.rpe = rpe; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
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

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getSchedulingNote() { return schedulingNote; }
    public void setSchedulingNote(String schedulingNote) { this.schedulingNote = schedulingNote; }
}