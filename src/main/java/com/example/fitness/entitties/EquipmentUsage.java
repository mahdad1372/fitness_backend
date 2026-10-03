package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

// One row = one physical unit of an equipment type currently checked out
// by a student (e.g. one of the two treadmills). Rows are inserted on
// "Start Using" and deleted on "Done" - COUNT(*) per equipment_id is the
// live "in use" number shown on the Equipment Board, mirroring how
// ChatController tracks activeUsers, but persisted instead of in-memory
// so it survives a server restart.
@Table(name = "equipment_usage")
@Entity
public class EquipmentUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer usage_id;

    @Column(nullable = false)
    private Integer equipment_id;

    @Column(nullable = false)
    private Integer user_id;

    @CreationTimestamp
    @Column(updatable = false, name = "started_at")
    private Date startedAt;

    public Integer getUsage_id() {
        return usage_id;
    }
    public void setUsage_id(Integer usage_id) {
        this.usage_id = usage_id;
    }
    public Integer getEquipment_id() {
        return equipment_id;
    }
    public void setEquipment_id(Integer equipment_id) {
        this.equipment_id = equipment_id;
    }
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public Date getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }
}