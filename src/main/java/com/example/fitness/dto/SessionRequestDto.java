package com.example.fitness.dto;

import java.util.Date;

// Display shape returned by GET endpoints and inside OptimizeScheduleResponse -
// resolves the raw student_id/coach_id/equipment_id foreign keys into names
// so the frontend doesn't need a second round trip.
public class SessionRequestDto {

    private Integer requestId;
    private Integer studentId;
    private String studentName;
    private Integer coachId;
    private String coachName;
    private Integer equipmentId;
    private String equipmentName;
    private Date startTime;
    private Date endTime;
    private Float price;
    private String status;
    // True when the requested end time had to be pulled in because it
    // exceeded this student's current cardiovascular-risk session ceiling
    // (Cardiovascular Risk -> Trainer & Equipment Scheduler).
    private boolean cappedForRisk;

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Integer getCoachId() { return coachId; }
    public void setCoachId(Integer coachId) { this.coachId = coachId; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCappedForRisk() { return cappedForRisk; }
    public void setCappedForRisk(boolean cappedForRisk) { this.cappedForRisk = cappedForRisk; }
}