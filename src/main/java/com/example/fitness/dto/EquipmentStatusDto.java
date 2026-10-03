package com.example.fitness.dto;

// Live snapshot of one equipment type for the Equipment Board - this is
// what the frontend polls every few seconds.
public class EquipmentStatusDto {

    private Integer equipmentId;
    private String name;
    private Integer capacity;
    private Integer inUse;
    private Integer available;
    private Integer queueLength;
    // Set only when the requesting user currently has a unit checked out,
    // so the frontend can show "Done" instead of "Start Using" for them.
    private boolean checkedInByMe;
    // Set only when the requesting user is in this equipment's queue, so the
    // frontend can show "Leave Queue (position N)" instead of "Join Queue".
    private Integer myQueuePosition;

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getInUse() { return inUse; }
    public void setInUse(Integer inUse) { this.inUse = inUse; }

    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }

    public Integer getQueueLength() { return queueLength; }
    public void setQueueLength(Integer queueLength) { this.queueLength = queueLength; }

    public boolean isCheckedInByMe() { return checkedInByMe; }
    public void setCheckedInByMe(boolean checkedInByMe) { this.checkedInByMe = checkedInByMe; }

    public Integer getMyQueuePosition() { return myQueuePosition; }
    public void setMyQueuePosition(Integer myQueuePosition) { this.myQueuePosition = myQueuePosition; }
}