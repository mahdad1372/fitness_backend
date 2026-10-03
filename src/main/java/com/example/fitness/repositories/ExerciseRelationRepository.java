package com.example.fitness.repositories;

import com.example.fitness.entitties.ExerciseRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRelationRepository extends JpaRepository<ExerciseRelation, Integer> {
    // Same underscore-parsing issue as ExerciseRepository.findByMuscle_groupIgnoreCase:
    // Spring Data's method-name parser splits "Relation_type" into "relation" + "type"
    // instead of matching the literal field name, so this is written as explicit JPQL.
    @Query("SELECT r FROM ExerciseRelation r WHERE r.relation_type = :type")
    List<ExerciseRelation> findByRelation_type(@Param("type") ExerciseRelation.RelationType type);
}