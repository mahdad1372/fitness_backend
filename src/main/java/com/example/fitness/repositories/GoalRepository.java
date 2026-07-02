package com.example.fitness.repositories;

import com.example.fitness.entitties.Goals;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface GoalRepository extends CrudRepository<Goals, Integer> {
    @Query(
            value = "SELECT * FROM Goals WHERE goal_id = ?1",
            nativeQuery = true
    )
    Goals findGoalsByGoal_id(Integer id);
    @Query(value="SELECT * FROM Goals a WHERE a.user_id=?1", nativeQuery=true)
    List<Goals> findGoalsByuser_ids(Integer user_id);
    @Query(value = "SELECT * FROM Goals" , nativeQuery = true)
    public List<Goals> getAllGoals();
    @Query(value = "    SELECT\n" +
            "        g.goal_id,\n" +
            "        g.goal_type,\n" +
            "        g.target_value,\n" +
            "        g.current_value,\n" +
            "        g.start_date,\n" +
            "        g.end_date,\n" +
            "        g.status,\n" +
            "        g.description,\n" +
            "        u.firstname,\n" +
            "        u.lastname,\n" +
            "        u.age,\n" +
            "        u.weight,\n" +
            "        u.height,\n" +
            "        u.role,\n" +
            "        u.user_id\n" +
            "    FROM goals g\n" +
            "    INNER JOIN users u\n" +
            "        ON g.user_id = u.user_id WHERE g.display_in_main_page = 1" , nativeQuery = true)
    public List diplayinmainpage();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Goals WHERE goal_id = :id", nativeQuery = true)
    public void deleteGoalsByGoal_id(Integer id);
    @Transactional
    @Modifying
    @Query(
            value =
                    "INSERT INTO Goals (" +
                            "user_id, goal_type, target_value, current_value, " +
                            "start_date, end_date, status, " +
                            "before_goal_image, after_goal_image ,display_in_main_page,description" +
                            ") VALUES (" +
                            "?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11" +
                            ")",
            nativeQuery = true
    )
    void addgoals(Integer user_id, String goal_type, Float target_value, Float current_value, Date start_date, Date end_date,
            String status, byte[] before_goal_image, byte[] after_goal_image, Integer display_in_main_page, String description
    );
    @Transactional
    @Modifying
    @Query(
            value =
                    "UPDATE Goals SET " +
                            "user_id = ?2, " +
                            "goal_type = ?3, " +
                            "target_value = ?4, " +
                            "current_value = ?5, " +
                            "start_date = ?6, " +
                            "end_date = ?7, " +
                            "status = ?8, " +
                            "before_goal_image = null, " +
                            "after_goal_image = null " +
                            "WHERE goal_id = ?1",
            nativeQuery = true
    )
    void updateGoal(
            Integer goal_id,Integer user_id,String goal_type, Float target_value, Float current_value, Date start_date, Date end_date,
            String status,
            byte[] before_goal_image,
            byte[] after_goal_image
    );

}

