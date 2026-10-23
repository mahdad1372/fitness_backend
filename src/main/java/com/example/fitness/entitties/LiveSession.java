package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Table(name = "live_sessions")
@Entity
public class LiveSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long item_id;

    @Column(nullable = false)
    private Integer student_id;

    @Column(nullable = false)
    private String exercise_name;

    @Column(nullable = false)
    private Integer duration_minutes;

    @Column(nullable = false)
    private Integer difficulty_level;

    // true = report steps (cardio/walking exercises), false = report reps (strength exercises)
    @Column(nullable = false)
    private Boolean track_steps;

    @Column(nullable = false)
    private String status; // IN_PROGRESS | COMPLETED

    @CreationTimestamp
    @Column(updatable = false, name = "started_at")
    private Date startedAt;

    @Column(name = "finished_at")
    private Date finishedAt;

    @Column(name = "pre_session_systolic")
    private Integer preSessionSystolic;

    @Column(name = "pre_session_diastolic")
    private Integer preSessionDiastolic;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItem_id() { return item_id; }
    public void setItem_id(Long item_id) { this.item_id = item_id; }
    public Integer getStudent_id() { return student_id; }
    public void setStudent_id(Integer student_id) { this.student_id = student_id; }
    public String getExercise_name() { return exercise_name; }
    public void setExercise_name(String exercise_name) { this.exercise_name = exercise_name; }
    public Integer getDuration_minutes() { return duration_minutes; }
    public void setDuration_minutes(Integer duration_minutes) { this.duration_minutes = duration_minutes; }
    public Integer getDifficulty_level() { return difficulty_level; }
    public void setDifficulty_level(Integer difficulty_level) { this.difficulty_level = difficulty_level; }
    public Boolean getTrack_steps() { return track_steps; }
    public void setTrack_steps(Boolean track_steps) { this.track_steps = track_steps; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartedAt() { return startedAt; }
    public Date getFinishedAt() { return finishedAt; }
    public void setFinished_at(Date finishedAt) { this.finishedAt = finishedAt; }
    public Integer getPreSessionSystolic() { return preSessionSystolic; }
    public void setPre_session_systolic(Integer v) { this.preSessionSystolic = v; }
    public Integer getPreSessionDiastolic() { return preSessionDiastolic; }
    public void setPre_session_diastolic(Integer v) { this.preSessionDiastolic = v; }
}