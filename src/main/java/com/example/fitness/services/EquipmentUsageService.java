package com.example.fitness.services;

import com.example.fitness.dto.EquipmentQueueEntryDto;
import com.example.fitness.dto.EquipmentStatusDto;
import com.example.fitness.entitties.Equipment;
import com.example.fitness.entitties.EquipmentQueue;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.EquipmentQueueRepository;
import com.example.fitness.repositories.EquipmentRepository;
import com.example.fitness.repositories.EquipmentUsageRepository;
import com.example.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// Live "gym floor" tracking for equipment: who's using what right now, and
// who's waiting for a free unit. This is a different concern from
// ResourceSchedulerService (which resolves ADVANCE bookings against coach +
// equipment conflicts) - this service is walk-up, real-time check-in/out,
// modeled directly on the same capacity + FIFO-waitlist + push-notification
// pattern ChatController/WaitingRoomService already use for the coach
// chatroom (capacity 2 there, "activeUsers" tracked explicitly, a
// SimpMessagingTemplate broadcast to /topic/notifications when a slot frees
// up). Here the same three pieces reappear: EquipmentUsage rows are the
// "activeUsers" (but persisted, and per equipment type with capacity > 1
// instead of hardcoded at 2), EquipmentQueue rows are the "waiting_room",
// and checkOut() fires the same kind of /topic/notifications broadcast.
@Service
@RequiredArgsConstructor
public class EquipmentUsageService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;
    private final EquipmentQueueRepository equipmentQueueRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<EquipmentStatusDto> status(Integer requestingUserId) {
        return StreamSupport.stream(equipmentRepository.findAll().spliterator(), false)
                .map(equipment -> toStatusDto(equipment, requestingUserId))
                .collect(Collectors.toList());
    }

    public void checkIn(Integer equipmentId, Integer userId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!equipmentUsageRepository.findByEquipmentIdAndUserId(equipmentId, userId).isEmpty()) {
            throw new RuntimeException("You already have this equipment checked out");
        }

        int inUse = countInUse(equipmentId);
        if (inUse >= equipment.getCapacity()) {
            throw new RuntimeException("All units of this equipment are in use - join the queue instead");
        }

        equipmentUsageRepository.addUsage(equipmentId, userId);
    }

    public void checkOut(Integer equipmentId, Integer userId) {
        if (equipmentUsageRepository.findByEquipmentIdAndUserId(equipmentId, userId).isEmpty()) {
            throw new RuntimeException("You don't have this equipment checked out");
        }
        equipmentUsageRepository.deleteByEquipmentIdAndUserId(equipmentId, userId);
        notifyNextInQueue(equipmentId);
    }

    public void joinQueue(Integer equipmentId, Integer userId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!equipmentUsageRepository.findByEquipmentIdAndUserId(equipmentId, userId).isEmpty()) {
            throw new RuntimeException("You already have this equipment checked out");
        }
        if (!equipmentQueueRepository.findByEquipmentIdAndUserId(equipmentId, userId).isEmpty()) {
            throw new RuntimeException("You're already in the queue for this equipment");
        }
        // A spot is actually free - no need to queue, check in directly.
        if (countInUse(equipmentId) < equipment.getCapacity()) {
            throw new RuntimeException("A unit is free right now - check in instead of queuing");
        }

        equipmentQueueRepository.addToQueue(equipmentId, userId);
    }

    public void leaveQueue(Integer equipmentId, Integer userId) {
        equipmentQueueRepository.deleteByEquipmentIdAndUserId(equipmentId, userId);
    }

    public List<EquipmentQueueEntryDto> queueFor(Integer equipmentId) {
        List<EquipmentQueue> queue = equipmentQueueRepository.findByEquipmentIdOrderByQueueIdAsc(equipmentId);
        List<EquipmentQueueEntryDto> result = new java.util.ArrayList<>();
        int position = 1;
        for (EquipmentQueue entry : queue) {
            EquipmentQueueEntryDto dto = new EquipmentQueueEntryDto();
            dto.setQueueId(entry.getQueue_id());
            dto.setEquipmentId(entry.getEquipment_id());
            dto.setUserId(entry.getUser_id());
            dto.setJoinedAt(entry.getJoinedAt());
            dto.setPosition(position++);
            userRepository.findById(entry.getUser_id())
                    .ifPresent(u -> dto.setUserName(u.getFirstname() + " " + u.getLastname()));
            result.add(dto);
        }
        return result;
    }

    // Pops the front of the queue (if any) and pushes the same kind of
    // "🔔 ... is free now" broadcast ChatController's removePeople() sends.
    // Every connected client receives it (matching the existing pattern);
    // it's up to each client's own poll of /api/equipment/status - via
    // myQueuePosition - to know whether the message was actually about them,
    // exactly like NotificationDropdown.tsx already checks "am I first in
    // the waiting list" before treating a chat-room-free push as its own.
    private void notifyNextInQueue(Integer equipmentId) {
        List<EquipmentQueue> queue = equipmentQueueRepository.findByEquipmentIdOrderByQueueIdAsc(equipmentId);
        if (queue.isEmpty()) {
            return;
        }
        EquipmentQueue next = queue.get(0);
        equipmentQueueRepository.deleteByQueueId(next.getQueue_id());

        String equipmentName = equipmentRepository.findById(equipmentId)
                .map(Equipment::getName)
                .orElse("Equipment");
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                "🔔 " + equipmentName + " is now available - you're up next!"
        );
    }

    private int countInUse(Integer equipmentId) {
        Integer count = equipmentUsageRepository.countByEquipmentId(equipmentId);
        return count == null ? 0 : count;
    }

    private EquipmentStatusDto toStatusDto(Equipment equipment, Integer requestingUserId) {
        EquipmentStatusDto dto = new EquipmentStatusDto();
        Integer equipmentId = equipment.getEquipment_id();
        int inUse = countInUse(equipmentId);
        Integer queueCount = equipmentQueueRepository.countByEquipmentId(equipmentId);

        dto.setEquipmentId(equipmentId);
        dto.setName(equipment.getName());
        dto.setCapacity(equipment.getCapacity());
        dto.setInUse(inUse);
        dto.setAvailable(Math.max(0, equipment.getCapacity() - inUse));
        dto.setQueueLength(queueCount == null ? 0 : queueCount);
        dto.setCheckedInByMe(!equipmentUsageRepository
                .findByEquipmentIdAndUserId(equipmentId, requestingUserId).isEmpty());

        List<EquipmentQueue> queue = equipmentQueueRepository.findByEquipmentIdOrderByQueueIdAsc(equipmentId);
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUser_id().equals(requestingUserId)) {
                dto.setMyQueuePosition(i + 1);
                break;
            }
        }
        return dto;
    }
}