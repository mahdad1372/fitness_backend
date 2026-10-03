package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

/**
 * A student's request for a coaching session at a specific time, tied to a
 * specific coach and (optionally) one unit of a shared equipment type.
 * Starts life as PENDING; the ResourceSchedulerService flips a batch of
 * these to ACCEPTED/REJECTED all at once when a coach/admin runs the
 * optimizer (see ResourceScheduler's Branch & Bound in the graph package).
 */
@Table(name = "session_requests")
@Entity
public class SessionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer request_id;

    @Column(nullable = false)
    private Integer student_id;

    // Named coach_id to match the existing coach_id-as-FK-to-User convention
    // already used by ChatRoom and Cart elsewhere in this codebase.
    @Column(nullable = false)
    private Integer coach_id;

    // Nullable - a session that needs no shared equipment (e.g. a pure
    // coaching/consultation slot) simply omits this.
    @Column(nullable = true)
    private Integer equipment_id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date start_time;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date end_time;

    @Column(nullable = false)
    private Float price;

    // PENDING | ACCEPTED | REJECTED
    @Column(nullable = false)
    private String status;

    // Set true when SessionRequestService.create() had to shorten this
    // request's end_time because it exceeded the student's current
    // cardiovascular-risk session ceiling (see
    // CardiovascularRiskService.peekSafeSessionCeilingMinutes). Defaults to
    // 0 so existing rows and any insert that predates this column read as
    // "not capped" rather than NULL.
    @Column(name = "capped_for_risk", nullable = false)
    private Integer cappedForRisk = 0;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public Integer getRequest_id() {
        return request_id;
    }
    public void setRequest_id(Integer request_id) {
        this.request_id = request_id;
    }
    public Integer getStudent_id() {
        return student_id;
    }
    public void setStudent_id(Integer student_id) {
        this.student_id = student_id;
    }
    public Integer getCoach_id() {
        return coach_id;
    }
    public void setCoach_id(Integer coach_id) {
        this.coach_id = coach_id;
    }
    public Integer getEquipment_id() {
        return equipment_id;
    }
    public void setEquipment_id(Integer equipment_id) {
        this.equipment_id = equipment_id;
    }
    public Date getStart_time() {
        return start_time;
    }
    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }
    public Date getEnd_time() {
        return end_time;
    }
    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }
    public Float getPrice() {
        return price;
    }
    public void setPrice(Float price) {
        this.price = price;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Integer getCappedForRisk() {
        return cappedForRisk;
    }
    public void setCappedForRisk(Integer cappedForRisk) {
        this.cappedForRisk = cappedForRisk;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}