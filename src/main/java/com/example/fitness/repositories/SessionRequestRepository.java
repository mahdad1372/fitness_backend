package com.example.fitness.repositories;

import com.example.fitness.entitties.SessionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRequestRepository extends CrudRepository<SessionRequest, Integer> {

    @Query(value = "SELECT * FROM session_requests a WHERE a.status = ?1", nativeQuery = true)
    List<SessionRequest> findByStatus(String status);

    @Query(value = "SELECT * FROM session_requests a WHERE a.coach_id = ?1", nativeQuery = true)
    List<SessionRequest> findByCoach_id(Integer coachId);

    @Query(value = "SELECT * FROM session_requests a WHERE a.student_id = ?1", nativeQuery = true)
    List<SessionRequest> findByStudent_id(Integer studentId);

    @Query(value = "SELECT * FROM session_requests a WHERE a.student_id = ?1 AND a.status = ?2 ORDER BY a.start_time ASC", nativeQuery = true)
    List<SessionRequest> findByStudent_idAndStatusOrderByStart_timeAsc(Integer studentId, String status);

    @Query(value = "SELECT * FROM session_requests a WHERE a.equipment_id = ?1 AND a.status = ?2", nativeQuery = true)
    List<SessionRequest> findByEquipment_idAndStatus(Integer equipmentId, String status);
}