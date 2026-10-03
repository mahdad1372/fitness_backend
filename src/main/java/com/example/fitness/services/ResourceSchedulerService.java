package com.example.fitness.services;

import com.example.fitness.dto.OptimizeScheduleResponse;
import com.example.fitness.dto.SessionRequestDto;
import com.example.fitness.entitties.Equipment;
import com.example.fitness.entitties.SessionRequest;
import com.example.fitness.graph.ResourceScheduler;
import com.example.fitness.repositories.EquipmentRepository;
import com.example.fitness.repositories.SessionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates one optimization pass: loads every currently PENDING
 * SessionRequest plus every Equipment type's capacity, converts them into
 * the graph package's plain ResourceScheduler.SessionRequest records
 * (start/end as minutes-since-epoch ints, since the algorithm itself has
 * no JPA/Spring dependency - same separation of concerns as MealPlanner and
 * MacroCalculator), runs the exact Branch & Bound search, then writes the
 * ACCEPTED/REJECTED verdict back onto each entity in one batch.
 */
@Service
@RequiredArgsConstructor
public class ResourceSchedulerService {

    private final SessionRequestRepository sessionRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final SessionRequestService sessionRequestService;

    public OptimizeScheduleResponse optimize() {
        List<SessionRequest> pending = sessionRequestRepository.findByStatus("PENDING");

        Map<Integer, Equipment> equipmentById = new HashMap<>();
        Map<String, Integer> capacities = new HashMap<>();
        for (Equipment e : equipmentRepository.findAll()) {
            equipmentById.put(e.getEquipment_id(), e);
            // Keyed by equipment_id as a string - the algorithm only cares
            // about grouping by "which shared resource", not the name.
            capacities.put(String.valueOf(e.getEquipment_id()), e.getCapacity());
        }

        Map<Integer, SessionRequest> byId = new LinkedHashMap<>();
        List<ResourceScheduler.SessionRequest> candidates = new ArrayList<>();
        for (SessionRequest r : pending) {
            byId.put(r.getRequest_id(), r);
            candidates.add(new ResourceScheduler.SessionRequest(
                    r.getRequest_id(),
                    null, // studentName not needed by the algorithm itself
                    "coach:" + r.getCoach_id(),
                    r.getEquipment_id() != null ? String.valueOf(r.getEquipment_id()) : null,
                    toMinutes(r.getStart_time()),
                    toMinutes(r.getEnd_time()),
                    r.getPrice()
            ));
        }

        ResourceScheduler.ScheduleResult result = ResourceScheduler.solve(candidates, capacities);

        Set<Integer> acceptedIds = result.accepted().stream()
                .map(ResourceScheduler.SessionRequest::id)
                .collect(Collectors.toSet());

        List<SessionRequest> toSave = new ArrayList<>();
        for (SessionRequest r : pending) {
            r.setStatus(acceptedIds.contains(r.getRequest_id()) ? "ACCEPTED" : "REJECTED");
            toSave.add(r);
        }
        sessionRequestRepository.saveAll(toSave);

        List<SessionRequestDto> acceptedDtos = toSave.stream()
                .filter(r -> "ACCEPTED".equals(r.getStatus()))
                .sorted(Comparator.comparing(SessionRequest::getStart_time))
                .map(sessionRequestService::toDto)
                .collect(Collectors.toList());
        List<SessionRequestDto> rejectedDtos = toSave.stream()
                .filter(r -> "REJECTED".equals(r.getStatus()))
                .sorted(Comparator.comparing(SessionRequest::getStart_time))
                .map(sessionRequestService::toDto)
                .collect(Collectors.toList());

        OptimizeScheduleResponse response = new OptimizeScheduleResponse();
        response.setAcceptedSessions(acceptedDtos);
        response.setRejectedSessions(rejectedDtos);
        response.setTotalRevenue(result.totalRevenue());
        response.setRequestsConsidered(pending.size());
        response.setNodesExplored(result.nodesExplored());
        return response;
    }

    private int toMinutes(Date date) {
        return (int) (date.getTime() / 60000L);
    }
}