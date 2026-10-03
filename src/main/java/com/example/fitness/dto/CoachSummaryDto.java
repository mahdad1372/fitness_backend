package com.example.fitness.dto;

// Minimal shape for populating a "pick a coach" dropdown when a student
// submits a session request - deliberately not the full User object.
public class CoachSummaryDto {

    private Integer coachId;
    private String name;

    public Integer getCoachId() { return coachId; }
    public void setCoachId(Integer coachId) { this.coachId = coachId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}