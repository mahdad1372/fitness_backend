package com.example.fitness.dto;

import java.util.Date;

// What a student submits to book a session - sits as PENDING until a coach
// or admin runs the optimizer. No coachId and no price here anymore: the
// coach is always this student's one active coach (billed as a flat
// monthly fee, not per session) and is filled in server-side by
// SessionRequestService.create() from User.coachId.
public class CreateSessionRequestDto {

    private Integer studentId;
    private Integer equipmentId;   // nullable - omit if no shared equipment is needed
    private Date startTime;
    private Date endTime;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
}