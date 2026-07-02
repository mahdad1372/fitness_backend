package com.example.fitness.repositories;
import com.example.fitness.entitties.Foods;
import com.example.fitness.entitties.User;
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
public interface FoodsRepository extends CrudRepository<Foods, Integer> {
    @Query(value="SELECT * FROM Foods a WHERE a.user_id=?1", nativeQuery=true)
    List<Foods> findByUsers_id(Integer activity_id);
    @Query(value="SELECT * FROM Foods a WHERE a.food_id=?1", nativeQuery=true)
    List<Foods> findByFood_id(Integer activity_id);
    @Query(value = "SELECT * FROM Foods" , nativeQuery = true)
    public List<Foods> getAllFoods();
    @Query("SELECT f FROM Foods f WHERE f.user_id = :userId")
    List<Foods> findByUserAndDate(@Param("userId") Integer userId,
                                  @Param("date") Date date);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Foods WHERE food_id = :id", nativeQuery = true)
    public void deleteFoodsByFood_id(Integer id);
    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO Foods (user_id, food_name, category, calories, protein,carbohydrates,fats,notes,meal_time) " +
                    "VALUES (?1, ?2, ?3, ?4, ?5,?6,?7,?8,?9)",
            nativeQuery = true
    )
    void addfoods(Integer user_id, String food_name, String category, Float calories, Float protein
            ,Float carbohydrates,Float fats,String notes,String meal_time);
    @Query(value="SELECT * FROM Foods a WHERE a.user_id=?1", nativeQuery=true)
    List<Foods> findByUser_id(Integer id);
    @Query(value = "UPDATE Foods SET food_name = ?2,category = ?3,calories = ?4,protein = ?5,carbohydrates = ?6," +
                    "fats = ?7,notes = ?8,meal_time = ?9 WHERE food_id = ?1",
            nativeQuery = true
    )
    void updateFood(
            Integer food_id,
            String food_name,
            String category,
            Float calories,
            Float protein,
            Float carbohydrates,
            Float fats,
            String notes,
            String meal_time
    );
}
