package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;


@Table(name = "daily_activities")
@Entity
public class Daily_activities {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer activity_id;
    @Column(nullable = false)
    private Integer user_id;
    @Column(nullable = false)
    private Integer steps;
    @Column(nullable = false)
    private Float sleep_hours;
    @Column(nullable = false)
    private Float water_intake;
    @Column(nullable = false)
    private Float calories_burned;
    @Column(nullable = false)
    private String mood ;
    @Column(nullable = false)
    private String notes ;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public Integer getSteps() {
        return steps;
    }
    public void setSteps(Integer steps) {
        this.steps = steps;
    }
    public Float getSleep_hours() {
        return sleep_hours;
    }
    public void setSleep_hours(Float sleep_hours) {
        this.sleep_hours = sleep_hours;
    }
    public Float getWater_intake() {
        return water_intake;
    }
    public void setWater_intake(Float water_intake) {
        this.water_intake = water_intake;
    }
    public Float getCalories_burned() {
        return calories_burned;
    }
    public void setCalories_burned(Float calories_burned) {
        this.calories_burned = calories_burned;
    }
    public String getMood() {
        return mood;
    }
    public void setMood(String mood) {
        this.mood = mood;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
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
    public Integer getActivity_id() {
        return activity_id;
    }
    public void setActivity_id(Integer activity_id) {
        this.activity_id = activity_id;
    }
}