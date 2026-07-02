package com.example.fitness.entitties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
@Table(name = "workouts")
@Entity
public class Workouts {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer workout_id;
    @Column(nullable = false)
    private Integer user_id;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private Integer duration ;
    @Column(nullable = false)
    private Float calories_burned ;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @Column(nullable = false)
    private Integer rest_seconds ;
    @Column(nullable = false)
    private Integer rpe ;
    @Column(nullable = false)
    private Float intensity_percent ;
    public Integer getWorkout_id() {
        return workout_id;
    }
    public void setWorkout_id(Integer workout_id) {
        this.workout_id = workout_id;
    }
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    public Float getCalories_burned() {
        return calories_burned;
    }
    public void setCalories_burned(Float calories_burned) {
        this.calories_burned = calories_burned;
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

    public Integer getRest_seconds() {
        return rest_seconds;
    }

    public Float getIntensity_percent() {
        return intensity_percent;
    }

    public Integer getRpe() {
        return rpe;
    }

    public void setIntensity_percent(Float intensity_percent) {
        this.intensity_percent = intensity_percent;
    }

    public void setRest_seconds(Integer rest_seconds) {
        this.rest_seconds = rest_seconds;
    }

    public void setRpe(Integer rpe) {
        this.rpe = rpe;
    }
}
