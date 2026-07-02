package com.example.fitness.repositories;
import com.example.fitness.entitties.Workouts;
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
public interface WorksoutsRepository extends CrudRepository<Workouts, Integer> {
    @Query(value="SELECT * FROM Workouts a WHERE a.user_id=?1", nativeQuery=true)
    List<Workouts> findWorkoutsByUser_id(Integer user_id);
    @Query(value="SELECT * FROM Workouts a WHERE a.workout_id=?1", nativeQuery=true)
    List<Workouts> findWorkoutsByWorkouts_id(Integer workout_id);
    @Query(value="SELECT * FROM workouts WHERE DATE(created_at) =?1 AND user_id=?2", nativeQuery=true)
    List<Workouts> findWorkoutsByday(String createdAt,Integer userId);
    @Query("SELECT w FROM Workouts w WHERE w.user_id = :userId")
    List<Workouts> findByUserAndDate(@Param("userId") Integer userId,
                                     @Param("date") Date date);
    @Query(value="SELECT * FROM workouts WHERE YEARWEEK(created_at, 1) = YEARWEEK(?1, 1) AND " +
            "user_id=?2", nativeQuery=true)
    List<Workouts> findWorkoutsByweek(String createdAt,Integer userId);

    @Query(value = "SELECT * FROM Workouts" , nativeQuery = true)
    public List<Workouts> getAllWorkouts();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Workouts WHERE workout_id = :id", nativeQuery = true)
    public void deleteWorkoutsByWorkout_id(Integer id);
    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO Workouts (user_id, type, duration, calories_burned,rest_seconds,rpe,intensity_percent) " +
                    "VALUES (?1, ?2, ?3, ?4,?5,?6,?7)",
            nativeQuery = true
    )
    void addworkouts(Integer user_id, String type, Integer duration, Float calories_burned,Integer rest_seconds,Integer rpe,Float intensity_percent);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE Workouts SET type = ?2, duration = ?3, calories_burned = ?4, rest_seconds = ?5, rpe = ?6, intensity_percent = ?7 WHERE workout_id = ?1",
            nativeQuery = true
    )
    void updateWorkoutsByWorkout_id(Integer workout_id, String type, Integer duration, Float calories_burned,Integer rest_seconds,Integer rpe,Float intensity_percent);

}
