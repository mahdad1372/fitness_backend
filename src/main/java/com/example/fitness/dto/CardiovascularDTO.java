package com.example.fitness.dto;

public class CardiovascularDTO {
    private Integer userId;
    private Integer age;
    private Boolean smoke; // keep as Integer
    private String gender;
    private Float cholesterol;
    private Float bloodPressure;
    private Float heartRate;

    // Constructor for @Query projection
    public CardiovascularDTO(Integer userId, Integer age, Boolean smoke, String gender,
                             Float cholesterol, Float bloodPressure, Float heartRate) {
        this.userId = userId;
        this.age = age;
        this.smoke = smoke;
        this.gender = gender;
        this.cholesterol = cholesterol;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
    }

    public Integer getAge() {
        return age;
    }

    public Float getBloodPressure() {
        return bloodPressure;
    }

    public Float getCholesterol() {
        return cholesterol;
    }

    public Float getHeartRate() {
        return heartRate;
    }

    public Boolean getSmoke() {
        return smoke;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getGender() {
        return gender;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setBloodPressure(Float bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public void setCholesterol(Float cholesterol) {
        this.cholesterol = cholesterol;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeartRate(Float heartRate) {
        this.heartRate = heartRate;
    }

    public void setSmoke(Boolean smoke) {
        this.smoke = smoke;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
