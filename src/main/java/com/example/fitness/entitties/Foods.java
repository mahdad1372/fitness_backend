package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Table(name = "foods")
@Entity
public class Foods {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer food_id;
    @Column(nullable = false)
    private Integer user_id;
    @Column(nullable = false)
    private String food_name ;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private Float calories;
    @Column(nullable = false)
    private Float protein;
    @Column(nullable = false)
    private Float carbohydrates;
    @Column(nullable = false)
    private Float fats;
    @Column(nullable = false)
    private String meal_time;
    @Column(nullable = false)
    private String notes;
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
    public String getFood_name() {
        return food_name;
    }
    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public Float getCalories() {
        return calories;
    }
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    public Float getProtein() {
        return protein;
    }
    public void setProtein(Float protein) {
        this.protein = protein;
    }
    public Float getCarbohydrates() {
        return carbohydrates;
    }
    public void setCarbohydrates(Float carbohydrates) {
        this.carbohydrates = carbohydrates;
    }
    public Float getFats() {
        return fats;
    }
    public void setFats(Float fats) {
        this.fats = fats;
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
    public Integer getFood_id() {
        return food_id;
    }
    public void setFood_id(Integer food_id) {
        this.food_id = food_id;
    }

    public String getMeal_time() {
        return meal_time;
    }
    public void setMeal_time(String meal_time) {
        this.meal_time = meal_time;
    }
}
