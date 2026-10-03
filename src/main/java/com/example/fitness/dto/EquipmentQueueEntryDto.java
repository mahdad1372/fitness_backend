package com.example.fitness.dto;

import java.util.Date;

public class EquipmentQueueEntryDto {

    private Integer queueId;
    private Integer equipmentId;
    private Integer userId;
    private String userName;
    private Date joinedAt;
    // 1-based - "you are position 1" means "you're next".
    private Integer position;

    public Integer getQueueId() { return queueId; }
    public void setQueueId(Integer queueId) { this.queueId = queueId; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}