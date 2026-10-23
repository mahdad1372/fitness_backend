package com.example.fitness.dto;

// Shape for browsing available coaches when a student is choosing/changing
// their ONE active coach. There is no fixed price catalog yet (coaches are
// priced per Cart row at subscribe time), so this is deliberately just the
// identity - not the full User object.
public class CoachSummaryDto {

    private Integer coachId;
    private String name;

    public Integer getCoachId() { return coachId; }
    public void setCoachId(Integer coachId) { this.coachId = coachId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}