package com.example.fitness.repositories;

import com.example.fitness.entitties.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    // Explicit JPQL for the same reason as ExerciseRepository: the derived
    // method-name parser trips over the underscore in student_id.
    @Query("SELECT s FROM LiveSession s WHERE s.id = :id AND s.student_id = :studentId")
    Optional<LiveSession> findByIdForStudent(@Param("id") Long id, @Param("studentId") Integer studentId);
}