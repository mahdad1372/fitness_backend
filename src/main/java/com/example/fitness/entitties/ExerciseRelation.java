package com.example.fitness.entitties;

import jakarta.persistence.*;

@Table(name = "exercise_relations")
@Entity
public class ExerciseRelation {

    public enum RelationType { PROGRESSION, SUBSTITUTE }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer relation_id;

    @Column(nullable = false)
    private Integer from_exercise_id;

    @Column(nullable = false)
    private Integer to_exercise_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationType relation_type;

    // PROGRESSION: abs(difficulty(to) - difficulty(from)); SUBSTITUTE: usually small (0-1)
    @Column(nullable = false)
    private Integer weight;

    public Integer getRelation_id() {
        return relation_id;
    }

    public void setRelation_id(Integer relation_id) {
        this.relation_id = relation_id;
    }

    public Integer getFrom_exercise_id() {
        return from_exercise_id;
    }

    public void setFrom_exercise_id(Integer from_exercise_id) {
        this.from_exercise_id = from_exercise_id;
    }

    public Integer getTo_exercise_id() {
        return to_exercise_id;
    }

    public void setTo_exercise_id(Integer to_exercise_id) {
        this.to_exercise_id = to_exercise_id;
    }

    public RelationType getRelation_type() {
        return relation_type;
    }

    public void setRelation_type(RelationType relation_type) {
        this.relation_type = relation_type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}