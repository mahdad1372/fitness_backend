package com.example.fitness.services;

import com.example.fitness.dto.CoachSummaryDto;
import com.example.fitness.dto.CreateSessionRequestDto;
import com.example.fitness.dto.SessionRequestDto;
import com.example.fitness.entitties.Equipment;
import com.example.fitness.entitties.SessionRequest;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.EquipmentRepository;
import com.example.fitness.repositories.SessionRequestRepository;
import com.example.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionRequestService {

    private final SessionRequestRepository sessionRequestRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    // Cardiovascular Risk -> Trainer & Equipment Scheduler: this student's
    // current risk category caps how long a NEW session request is allowed
    // to run, BEFORE it's ever persisted as PENDING. The Branch & Bound
    // optimizer that later accepts/rejects requests never has to know
    // anything about risk - by the time it runs, every candidate request is
    // already risk-shaped. REMOVE this field (and the block in create()
    // below that uses it) if you haven't built Cardiovascular Risk yet.
    private final CardiovascularRiskService cardiovascularRiskService;

    public SessionRequestDto create(CreateSessionRequestDto request) {
        User coach = userRepository.findById(request.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach not found"));
        if (!"COACH".equalsIgnoreCase(coach.getRole())) {
            throw new RuntimeException("Requested coachId does not belong to a COACH-role user");
        }
        userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (request.getEquipmentId() != null) {
            equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found"));
        }
        if (!request.getEndTime().after(request.getStartTime())) {
            throw new RuntimeException("endTime must be after startTime");
        }

        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        boolean cappedForRisk = false;

        Optional<Integer> ceilingMinutes = cardiovascularRiskService.peekSafeSessionCeilingMinutes(request.getStudentId());
        if (ceilingMinutes.isPresent()) {
            long requestedMinutes = (endTime.getTime() - startTime.getTime()) / 60000L;
            if (requestedMinutes > ceilingMinutes.get()) {
                endTime = new Date(startTime.getTime() + ceilingMinutes.get() * 60000L);
                cappedForRisk = true;
            }
        }

        SessionRequest entity = new SessionRequest();
        entity.setStudent_id(request.getStudentId());
        entity.setCoach_id(request.getCoachId());
        entity.setEquipment_id(request.getEquipmentId());
        entity.setStart_time(startTime);
        entity.setEnd_time(endTime);
        entity.setPrice(request.getPrice());
        entity.setStatus("PENDING");
        entity.setCappedForRisk(cappedForRisk ? 1 : 0);
        sessionRequestRepository.save(entity);
        return toDto(entity);
    }

    public List<SessionRequestDto> listByStatus(String status) {
        return sessionRequestRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionRequestDto> listByCoach(Integer coachId) {
        return sessionRequestRepository.findByCoach_id(coachId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SessionRequestDto> listByStudent(Integer studentId) {
        return sessionRequestRepository.findByStudent_id(studentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<CoachSummaryDto> listCoaches() {
        return userRepository.findByRole("COACH").stream()
                .map(u -> {
                    CoachSummaryDto dto = new CoachSummaryDto();
                    dto.setCoachId(u.getUser_id());
                    dto.setName(u.getFirstname() + " " + u.getLastname());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Resolves student/coach/equipment names for display - kept here rather
    // than in ResourceSchedulerService since both need it (before AND after
    // optimizing, the frontend shows the same shape).
    SessionRequestDto toDto(SessionRequest entity) {
        SessionRequestDto dto = new SessionRequestDto();
        dto.setRequestId(entity.getRequest_id());
        dto.setStudentId(entity.getStudent_id());
        dto.setCoachId(entity.getCoach_id());
        dto.setEquipmentId(entity.getEquipment_id());
        dto.setStartTime(entity.getStart_time());
        dto.setEndTime(entity.getEnd_time());
        dto.setPrice(entity.getPrice());
        dto.setStatus(entity.getStatus());
        dto.setCappedForRisk(entity.getCappedForRisk() != null && entity.getCappedForRisk() == 1);

        userRepository.findById(entity.getStudent_id())
                .ifPresent(u -> dto.setStudentName(u.getFirstname() + " " + u.getLastname()));
        userRepository.findById(entity.getCoach_id())
                .ifPresent(u -> dto.setCoachName(u.getFirstname() + " " + u.getLastname()));
        if (entity.getEquipment_id() != null) {
            Optional<Equipment> equipment = equipmentRepository.findById(entity.getEquipment_id());
            equipment.ifPresent(e -> dto.setEquipmentName(e.getName()));
        }
        return dto;
    }
}