package com.example.fitness.repositories;

import com.example.fitness.entitties.EquipmentUsage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentUsageRepository extends CrudRepository<EquipmentUsage, Integer> {

    @Query(
            value = "SELECT * FROM equipment_usage WHERE equipment_id = ?1",
            nativeQuery = true
    )
    List<EquipmentUsage> findByEquipmentId(Integer equipmentId);

    @Query(
            value = "SELECT COUNT(*) FROM equipment_usage WHERE equipment_id = ?1",
            nativeQuery = true
    )
    Integer countByEquipmentId(Integer equipmentId);

    @Query(
            value = "SELECT * FROM equipment_usage WHERE equipment_id = ?1 AND user_id = ?2",
            nativeQuery = true
    )
    List<EquipmentUsage> findByEquipmentIdAndUserId(Integer equipmentId, Integer userId);

    @Query(
            value = "SELECT * FROM equipment_usage WHERE user_id = ?1",
            nativeQuery = true
    )
    List<EquipmentUsage> findByUserId(Integer userId);

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO equipment_usage (equipment_id, user_id) VALUES (?1, ?2)",
            nativeQuery = true
    )
    void addUsage(Integer equipmentId, Integer userId);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM equipment_usage WHERE equipment_id = ?1 AND user_id = ?2",
            nativeQuery = true
    )
    void deleteByEquipmentIdAndUserId(Integer equipmentId, Integer userId);
}