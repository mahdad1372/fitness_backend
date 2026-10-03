package com.example.fitness.dto;

import java.util.Date;

// What a student submits to book a session - sits as PENDING until a coach
// or admin runs the optimizer (POST /api/resource-scheduler/optimize).
public class CreateSessionRequestDto {

    private Integer studentId;
    private Integer coachId;
    private Integer equipmentId;   // nullable - omit if no shared equipment is needed
    private Date startTime;
    private Date endTime;
    private Float price;

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getCoachId() { return coachId; }
    public void setCoachId(Integer coachId) { this.coachId = coachId; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
}