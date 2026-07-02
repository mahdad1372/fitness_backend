package com.example.fitness.repositories;
import com.example.fitness.entitties.Daily_activities;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyactivityRepository extends CrudRepository<Daily_activities, Integer> {
    @Query(value="SELECT * FROM daily_activities a WHERE a.activity_id=?1", nativeQuery=true)
    Optional<Daily_activities> findByActivity_id(Integer activity_id);

    @Query(value = "SELECT * FROM daily_activities" , nativeQuery = true)
    public List<Daily_activities> getaAllDailyActivities();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM daily_activities WHERE activity_id = :id", nativeQuery = true)
    public void deleteDaily_activitiesById(Integer id);
    @Query("SELECT d FROM Daily_activities d WHERE d.user_id = :userId")
    List<Daily_activities> findByUserAndDate(@Param("userId") Integer userId,
                                             @Param("date") Date date);
    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO daily_activities (user_id, steps, sleep_hours, mood, notes,water_intake,calories_burned) " +
                "VALUES (?1, ?2, ?3, ?4, ?5,?6,?7)",
        nativeQuery = true
)
    void addDailyActivity(Integer user_id, Integer steps, Float sleep_hours, String mood, String notes
    ,Float water_intake,Float calories_burned);

}
