package com.example.fitness.repositories;

import com.example.fitness.entitties.EquipmentQueue;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentQueueRepository extends CrudRepository<EquipmentQueue, Integer> {

    // Ordered by queue_id (insertion order) so index 0 is always "who's next" -
    // same FIFO-by-insertion-order convention as WaitingRoomRepository.
    @Query(
            value = "SELECT * FROM equipment_queue WHERE equipment_id = ?1 ORDER BY queue_id ASC",
            nativeQuery = true
    )
    List<EquipmentQueue> findByEquipmentIdOrderByQueueIdAsc(Integer equipmentId);

    @Query(
            value = "SELECT * FROM equipment_queue WHERE equipment_id = ?1 AND user_id = ?2",
            nativeQuery = true
    )
    List<EquipmentQueue> findByEquipmentIdAndUserId(Integer equipmentId, Integer userId);

    @Query(
            value = "SELECT COUNT(*) FROM equipment_queue WHERE equipment_id = ?1",
            nativeQuery = true
    )
    Integer countByEquipmentId(Integer equipmentId);

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO equipment_queue (equipment_id, user_id) VALUES (?1, ?2)",
            nativeQuery = true
    )
    void addToQueue(Integer equipmentId, Integer userId);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM equipment_queue WHERE queue_id = ?1",
            nativeQuery = true
    )
    void deleteByQueueId(Integer queueId);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM equipment_queue WHERE equipment_id = ?1 AND user_id = ?2",
            nativeQuery = true
    )
    void deleteByEquipmentIdAndUserId(Integer equipmentId, Integer userId);
}