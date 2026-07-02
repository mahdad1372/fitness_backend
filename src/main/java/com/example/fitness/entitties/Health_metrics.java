package com.example.fitness.entitties;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;

@Table(name = "health_metrics")
@Entity
public class Health_metrics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer id;
    @Column(nullable = false)
    private Integer user_id;
    @Column(nullable = false)
    private Float cholesterol;
    @Column(nullable = false)
    private Float blood_pressure;
    @Column(nullable = false)
    private Float heart_rate;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public Float getCholesterol() {
        return cholesterol;
    }
    public void setCholesterol(Float cholesterol) {
        this.cholesterol = cholesterol;
    }
    public Float getBlood_pressure() {
        return blood_pressure;
    }
    public void setBlood_pressure(Float blood_pressure) {
        this.blood_pressure = blood_pressure;
    }
    public Float getHeart_rate() {
        return heart_rate;
    }
    public void setHeart_rate(Float heart_rate) {
        this.heart_rate = heart_rate;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

}
