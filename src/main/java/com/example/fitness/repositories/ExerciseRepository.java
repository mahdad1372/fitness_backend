package com.example.fitness.repositories;

import com.example.fitness.entitties.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    // Written as an explicit JPQL query instead of a derived method name:
    // Spring Data's method-name parser treats the underscore in "muscle_group"
    // as a manual property-traversal separator when combined with IgnoreCase,
    // and splits it into "muscle" + "group" instead of matching the literal
    // field name, which fails with "No property 'muscle' found for type 'Exercise'".
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.muscle_group) = LOWER(:muscleGroup)")
    List<Exercise> findByMuscle_groupIgnoreCase(@Param("muscleGroup") String muscleGroup);
}