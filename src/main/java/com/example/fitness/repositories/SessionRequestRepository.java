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

    // Used by WeeklyPlannerService to find this student's next booked
    // coaching session (if the Trainer & Equipment Scheduler has accepted
    // one) - ordered soonest-first so the caller can just take the first
    // one that's still in the future.
    @Query(value = "SELECT * FROM session_requests a WHERE a.student_id = ?1 AND a.status = ?2 ORDER BY a.start_time ASC", nativeQuery = true)
    List<SessionRequest> findByStudent_idAndStatusOrderByStart_timeAsc(Integer studentId, String status);
}