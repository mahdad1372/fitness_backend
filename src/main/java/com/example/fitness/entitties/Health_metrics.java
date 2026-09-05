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
    private Float body_temperature;
    @Column(nullable = false)
    private Float spo2;
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
    public Float getBody_temperature() {
        return body_temperature;
    }
    public void setBody_temperature(Float body_temperature) {
        this.body_temperature = body_temperature;
    }
    public Float getSpo2() {
        return spo2;
    }
    public void setSpo2(Float spo2) {
        this.spo2 = spo2;
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