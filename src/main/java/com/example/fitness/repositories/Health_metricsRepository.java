package com.example.fitness.repositories;
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
            value = "INSERT INTO Health_metrics (user_id, cholesterol, body_temperature, spo2) " +
                    "VALUES (?1, ?2, ?3, ?4)",
            nativeQuery = true
    )
    void addHealth_metrics(Integer user_id, Float cholesterol, Float body_temperature, Float spo2);
    @Query(value = "SELECT * FROM Health_metrics" , nativeQuery = true)
    public List<Health_metrics> getallHealthMetrics();
    @Query(value="SELECT * FROM Health_metrics a WHERE a.user_id=?1", nativeQuery=true)
    List<Health_metrics> findByUser_id(Integer id);
    @Query("SELECT h FROM Health_metrics h WHERE h.user_id = :userId")
    List<Health_metrics> findByUserAndDate(@Param("userId") Integer userId,
                                           @Param("date") Date date);

    @Query(value = "UPDATE Health_metrics SET cholesterol = ?2,body_temperature = ?3,spo2 = ?4  WHERE id = ?1",
            nativeQuery = true
    )
    void Healthmetric_update(
            Integer id, Float cholesterol, Float body_temperature, Float spo2
    );
    @Transactional
    @Modifying
    @Query(value = "UPDATE Health_metrics SET hdl_cholesterol = ?2 WHERE id = ?1", nativeQuery = true)
    void updateHdlCholesterol(Integer id, Float hdlCholesterol);
    @Query(value="SELECT * FROM Health_metrics a WHERE a.id=?1", nativeQuery=true)
    public List<Health_metrics> findBy_id(Integer id);
    @Query(
            value = "DELETE FROM Health_metrics WHERE id = :id",
            nativeQuery = true
    )
    void deleteHealth_metricsByhealth_id(@Param("id") Integer id);
}