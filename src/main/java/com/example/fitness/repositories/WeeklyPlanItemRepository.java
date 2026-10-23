package com.example.fitness.repositories;

import com.example.fitness.entitties.WeeklyPlanItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WeeklyPlanItemRepository extends CrudRepository<WeeklyPlanItem, Integer> {

    @Query(value = "SELECT * FROM weekly_plan_items a WHERE a.plan_id = ?1 ORDER BY a.item_order ASC", nativeQuery = true)
    List<WeeklyPlanItem> findByPlan_idOrderByItem_orderAsc(Integer planId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM weekly_plan_items a WHERE a.plan_id = ?1", nativeQuery = true)
    void deleteByPlan_id(Integer planId);
    @Modifying
    @Transactional
    @Query(value = "UPDATE weekly_plan_items a SET a.completed = ?2 WHERE a.item_id = ?1", nativeQuery = true)
    void updateCompleted(Integer itemId, Integer completed);
}