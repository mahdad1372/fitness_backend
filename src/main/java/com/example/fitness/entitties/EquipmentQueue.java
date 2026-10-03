package com.example.fitness.entitties;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

// FIFO waitlist entry for one equipment type, once every physical unit is
// checked out. Structurally identical to WaitingRoom (chatroom_id, user_id)
// - same idea, just keyed by equipment_id instead of chatroom_id. Ordering
// by queue_id (insertion order) is what makes "who's next" well-defined.
@Table(name = "equipment_queue")
@Entity
public class EquipmentQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer queue_id;

    @Column(nullable = false)
    private Integer equipment_id;

    @Column(nullable = false)
    private Integer user_id;

    @CreationTimestamp
    @Column(updatable = false, name = "joined_at")
    private Date joinedAt;

    public Integer getQueue_id() {
        return queue_id;
    }
    public void setQueue_id(Integer queue_id) {
        this.queue_id = queue_id;
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
    public Date getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }
}