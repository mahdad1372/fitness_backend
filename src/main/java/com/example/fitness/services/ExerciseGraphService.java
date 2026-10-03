package com.example.fitness.services;

import com.example.fitness.entitties.Exercise;
import com.example.fitness.entitties.ExerciseRelation;
import com.example.fitness.graph.ExerciseGraph;
import com.example.fitness.repositories.ExerciseRepository;
import com.example.fitness.repositories.ExerciseRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseGraphService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseRelationRepository relationRepository;

    @Cacheable("exerciseGraph")
    public ExerciseGraph buildGraph() {
        ExerciseGraph graph = new ExerciseGraph();
        for (Exercise ex : exerciseRepository.findAll()) {
            graph.addNode(ex.getExercise_id());
        }
        for (ExerciseRelation rel : relationRepository.findAll()) {
            ExerciseGraph.EdgeType type = rel.getRelation_type() == ExerciseRelation.RelationType.PROGRESSION
                    ? ExerciseGraph.EdgeType.PROGRESSION
                    : ExerciseGraph.EdgeType.SUBSTITUTE;
            graph.addEdge(rel.getFrom_exercise_id(), rel.getTo_exercise_id(), type, rel.getWeight());
        }
        return graph;
    }

    @CacheEvict(value = "exerciseGraph", allEntries = true)
    public void clearGraphCache() {
        // call this after any admin write to Exercise / ExerciseRelation
    }

    public List<Exercise> findSubstitutes(Integer exerciseId, Set<String> unavailableEquipment, int maxResults) {
        Set<Integer> excluded = exerciseRepository.findAll().stream()
                .filter(e -> e.getEquipment_required() != null
                        && unavailableEquipment.contains(e.getEquipment_required()))
                .map(Exercise::getExercise_id)
                .collect(Collectors.toSet());

        List<Integer> ids = buildGraph().findSubstitutes(exerciseId, excluded, maxResults);
        return exerciseRepository.findAllById(ids);
    }

    public List<Exercise> getProgressionOrder(String muscleGroup) {
        Set<Integer> subgraphNodes = exerciseRepository.findByMuscle_groupIgnoreCase(muscleGroup).stream()
                .map(Exercise::getExercise_id)
                .collect(Collectors.toSet());

        List<Integer> orderedIds = buildGraph().progressionOrder(subgraphNodes);
        Map<Integer, Exercise> byId = exerciseRepository.findAllById(orderedIds).stream()
                .collect(Collectors.toMap(Exercise::getExercise_id, e -> e));
        return orderedIds.stream().map(byId::get).toList();
    }

    public Optional<ExerciseGraph.PathResult> findSmoothestPath(Integer fromExerciseId, Integer toExerciseId) {
        return buildGraph().smoothestPath(fromExerciseId, toExerciseId);
    }
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }
}