package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "equipment")
@Entity
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer equipment_id;

    @Column(nullable = false)
    private String name;

    // How many identical units of this equipment type the studio owns
    // (e.g. 2 squat racks, 3 treadmills) - this is exactly the "capacity"
    // fed into the ResourceScheduler's knapsack-style resource constraint.
    @Column(nullable = false)
    private Integer capacity;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    public Integer getEquipment_id() {
        return equipment_id;
    }
    public void setEquipment_id(Integer equipment_id) {
        this.equipment_id = equipment_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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