package com.example.fitness.repositories;
import com.example.fitness.dto.CardiovascularDTO;
import com.example.fitness.entitties.Health_metrics;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Repository
public interface Health_metricsRepository extends CrudRepository<Health_metrics, Integer> {
    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO Health_metrics (user_id, cholesterol, blood_pressure, heart_rate) " +
                    "VALUES (?1, ?2, ?3, ?4)",
            nativeQuery = true
    )
    void addHealth_metrics(Integer user_id, Float cholesterol, Float blood_pressure, Float heart_rate);
    @Query(value="SELECT u.user_id, u.age, u.smoke, u.gender,\n" +
            "       h.cholesterol, h.blood_pressure, h.heart_rate\n" +
            "FROM users u\n" +
            "LEFT JOIN (\n" +
            "    SELECT *\n" +
            "    FROM health_metrics\n" +
            "    WHERE user_id = 1\n" +
            "    ORDER BY created_at DESC\n" +
            "    FETCH FIRST 1 ROW ONLY\n" +
            ") h\n" +
            "ON u.user_id = h.user_id\n" +
            "WHERE u.user_id = 1", nativeQuery=true)

    List<CardiovascularDTO> cardiovascular(Integer userId);
    @Query(value = "SELECT * FROM Health_metrics" , nativeQuery = true)
    public List<Health_metrics> getallHealthMetrics();
    @Query(value="SELECT * FROM Health_metrics a WHERE a.user_id=?1", nativeQuery=true)
    List<Health_metrics> findByUser_id(Integer id);
    @Query("SELECT h FROM Health_metrics h WHERE h.user_id = :userId")
    List<Health_metrics> findByUserAndDate(@Param("userId") Integer userId,
                                           @Param("date") Date date);

    @Query(value = "UPDATE Health_metrics SET cholesterol = ?2,blood_pressure = ?3,heart_rate = ?4  WHERE id = ?1",
            nativeQuery = true
    )
    void Healthmetric_update(
            Integer id, Float cholesterol, Float blood_pressure, Float heart_rate
    );
    @Query(value="SELECT * FROM Health_metrics a WHERE a.id=?1", nativeQuery=true)
    public List<Health_metrics> findBy_id(Integer id);
    @Query(
            value = "DELETE FROM Health_metrics WHERE id = :id",
            nativeQuery = true
    )
    void deleteHealth_metricsByhealth_id(@Param("id") Integer id);
}
