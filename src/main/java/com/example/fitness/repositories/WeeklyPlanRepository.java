package com.example.fitness.repositories;

import com.example.fitness.entitties.WeeklyPlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface WeeklyPlanRepository extends CrudRepository<WeeklyPlan, Integer> {

    @Query(value = "SELECT * FROM weekly_plans a WHERE a.student_id = ?1 AND a.week_start_date = ?2", nativeQuery = true)
    List<WeeklyPlan> findByStudent_idAndWeek_start_date(Integer studentId, Date weekStartDate);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM weekly_plans a WHERE a.student_id = ?1 AND a.week_start_date = ?2", nativeQuery = true)
    void deleteByStudent_idAndWeek_start_date(Integer studentId, Date weekStartDate);
    @Query(value = "SELECT * FROM weekly_plans a WHERE a.student_id = ?1 ORDER BY a.generated_at DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    List<WeeklyPlan> findLatestByStudent_id(Integer studentId);
}