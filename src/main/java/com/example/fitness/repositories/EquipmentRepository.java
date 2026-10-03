package com.example.fitness.repositories;

import com.example.fitness.entitties.Equipment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends CrudRepository<Equipment, Integer> {
}